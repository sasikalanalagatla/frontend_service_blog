package com.mb.frontendService.controller;

import com.mb.frontendService.dto.CommentDto;
import com.mb.frontendService.dto.PostDto;
import com.mb.frontendService.dto.UserDto;
import com.mb.frontendService.feign.CommentServiceClient;
import com.mb.frontendService.feign.PostServiceClient;
import com.mb.frontendService.feign.UserServiceClient;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;


@Controller
public class BlogController {

    @Autowired
    private PostServiceClient postServiceClient;

    @Autowired
    private CommentServiceClient commentServiceClient;

    @Autowired
    private UserServiceClient userServiceClient;

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "1") int page,
                      @RequestParam(defaultValue = "newest") String sort,
                      @RequestParam(required = false) String tag,
                      @RequestParam(required = false) String keyword,
                      Model model,
                      HttpSession session) {
        try {
            List<PostDto> allPublishedPosts;
            
            if (keyword != null && !keyword.trim().isEmpty()) {
                List<PostDto> posts = postServiceClient.searchPosts(keyword.trim());
                List<PostDto> tagPosts = postServiceClient.getPostsByTag(keyword.trim());
                List<PostDto> allPosts = postServiceClient.getAllPosts();

                List<Long> seenIds = new ArrayList<>();
                List<PostDto> allResults = new ArrayList<>();

                for (PostDto post : posts) {
                    if (post != null && post.getId() != null && !seenIds.contains(post.getId())) {
                        allResults.add(post);
                        seenIds.add(post.getId());
                    }
                }
                for (PostDto post : tagPosts) {
                    if (post != null && post.getId() != null && !seenIds.contains(post.getId())) {
                        allResults.add(post);
                        seenIds.add(post.getId());
                    }
                }
                String lower = keyword.toLowerCase();
                for (PostDto post : allPosts) {
                    String author = post.getAuthor();
                    if (author != null && author.toLowerCase().contains(lower)) {
                        if (post.getId() != null && !seenIds.contains(post.getId())) {
                            allResults.add(post);
                            seenIds.add(post.getId());
                        }
                    }
                }
                allPublishedPosts = allResults;
                
                if (tag != null && !tag.trim().isEmpty()) {
                    List<PostDto> filteredResults = new ArrayList<>();
                    for (PostDto post : allPublishedPosts) {
                        if (post.getTags() != null) {
                            for (String postTag : post.getTags()) {
                                if (tag.trim().equalsIgnoreCase(postTag)) {
                                    filteredResults.add(post);
                                    break;
                                }
                            }
                        }
                    }
                    allPublishedPosts = filteredResults;
                }
            } else if (tag != null && !tag.trim().isEmpty()) {
                allPublishedPosts = postServiceClient.getPostsByTag(tag.trim());
            } else {
                allPublishedPosts = postServiceClient.getPublishedPosts();
            }
            
            List<PostDto> sortedPosts = sortPosts(allPublishedPosts, sort);
            int postsPerPage = 10;
            int totalPosts = sortedPosts.size();
            int totalPages = (int) Math.ceil((double) totalPosts / postsPerPage);
            if (totalPages == 0) {
                totalPages = 1;
            }
            if (page < 1) page = 1;
            if (page > totalPages) page = totalPages;
            int startIndex = (page - 1) * postsPerPage;
            int endIndex = Math.min(startIndex + postsPerPage, totalPosts);
            List<PostDto> postsForPage = sortedPosts.subList(startIndex, endIndex);

            List<String> allTags = postServiceClient.getAllTags();

            model.addAttribute("posts", postsForPage);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("totalPosts", totalPosts);
            model.addAttribute("sort", sort);
            model.addAttribute("selectedTag", tag);
            model.addAttribute("keyword", keyword);
            model.addAttribute("allTags", allTags);

            UserDto currentUser = (UserDto) session.getAttribute("user");
            model.addAttribute("currentUser", currentUser);

            return "home";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load posts: " + e.getMessage());
            return "error";
        }
    }

    private List<PostDto> sortPosts(List<PostDto> posts, String sort) {
        List<PostDto> copy = new ArrayList<>();
        for (PostDto post : posts) {
            copy.add(post);
        }
        
        if ("oldest".equalsIgnoreCase(sort)) {
            for (int i = 0; i < copy.size() - 1; i++) {
                for (int j = 0; j < copy.size() - i - 1; j++) {
                    if (copy.get(j).getCreatedAt().compareTo(copy.get(j + 1).getCreatedAt()) > 0) {
                        PostDto temp = copy.get(j);
                        copy.set(j, copy.get(j + 1));
                        copy.set(j + 1, temp);
                    }
                }
            }
        } else if ("title".equalsIgnoreCase(sort)) {
            for (int i = 0; i < copy.size() - 1; i++) {
                for (int j = 0; j < copy.size() - i - 1; j++) {
                    String title1 = copy.get(j).getTitle();
                    String title2 = copy.get(j + 1).getTitle();
                    if (title1 == null) title1 = "";
                    if (title2 == null) title2 = "";
                    if (title1.compareToIgnoreCase(title2) > 0) {
                        PostDto temp = copy.get(j);
                        copy.set(j, copy.get(j + 1));
                        copy.set(j + 1, temp);
                    }
                }
            }
        } else if ("author".equalsIgnoreCase(sort)) {
            for (int i = 0; i < copy.size() - 1; i++) {
                for (int j = 0; j < copy.size() - i - 1; j++) {
                    String author1 = copy.get(j).getAuthor();
                    String author2 = copy.get(j + 1).getAuthor();
                    if (author1 == null) author1 = "";
                    if (author2 == null) author2 = "";
                    if (author1.compareToIgnoreCase(author2) > 0) {
                        PostDto temp = copy.get(j);
                        copy.set(j, copy.get(j + 1));
                        copy.set(j + 1, temp);
                    }
                }
            }
        } else {
            for (int i = 0; i < copy.size() - 1; i++) {
                for (int j = 0; j < copy.size() - i - 1; j++) {
                    if (copy.get(j).getCreatedAt().compareTo(copy.get(j + 1).getCreatedAt()) < 0) {
                        PostDto temp = copy.get(j);
                        copy.set(j, copy.get(j + 1));
                        copy.set(j + 1, temp);
                    }
                }
            }
        }
        return copy;
    }

    @GetMapping("/posts/{postId}")
    public String viewPost(@PathVariable Long postId, Model model, HttpSession session) {
        try {
            PostDto post = postServiceClient.getPostById(postId);
            List<CommentDto> comments = commentServiceClient.getCommentsByPostId(postId);
            model.addAttribute("post", post);
            model.addAttribute("comments", comments);
            UserDto currentUser = (UserDto) session.getAttribute("user");
            model.addAttribute("currentUser", currentUser);
            return "view_post";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load post: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/users/{id}")
    public String userProfile(@PathVariable Long id, Model model, HttpSession session) {
        try {
            UserDto user = userServiceClient.getUserById(id);
            List<PostDto> userPosts = postServiceClient.getPostsByAuthor(user.getName());
            List<CommentDto> userComments = new ArrayList<>(); // Empty list for now
            model.addAttribute("user", user);
            model.addAttribute("posts", userPosts);
            model.addAttribute("comments", userComments);
            UserDto currentUser = (UserDto) session.getAttribute("user");
            model.addAttribute("currentUser", currentUser);
            return "user_profile";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load user profile: " + e.getMessage());
            return "error";
        }
    }

    @GetMapping("/posts/create")
    public String createPostForm(Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        PostDto post = new PostDto();
        post.setPublished(true);
        post.setAuthor(currentUser.getName());
        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        return "create_post";
    }

    @PostMapping("/posts")
    public String submitCreatePost(@ModelAttribute("post") PostDto post,
                                   @RequestParam(value = "tagsCsv", required = false) String tagsCsv,
                                   Model model,
                                   HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        try {
            if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
                List<String> tags = new ArrayList<>();
                String[] parts = tagsCsv.split(",");
                for (String part : parts) {
                    String trimmedTag = part.trim();
                    if (!trimmedTag.isEmpty()) {
                        tags.add(trimmedTag);
                    }
                }
                post.setTags(tags);
            } else {
                post.setTags(new ArrayList<>());
            }
            
            if (!"ADMIN".equals(currentUser.getRole())) {
                post.setAuthor(currentUser.getName());
            }
            
            if (post.isPublished()) {
                post.setPublishedAt(LocalDateTime.now());
            }
            
            postServiceClient.createPost(post);
            return "redirect:/";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "Unable to create post: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            return "error";
        }
    }

    @GetMapping("/posts/{id}/edit")
    public String editPostForm(@PathVariable Long id, Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        try {
            PostDto post = postServiceClient.getPostById(id);
            if (!post.getAuthor().equals(currentUser.getName()) && !"ADMIN".equals(currentUser.getRole())) {
                return "redirect:/posts/" + id;
            }
            
            String tagsCsv = "";
            if (post.getTags() != null) {
                for (int i = 0; i < post.getTags().size(); i++) {
                    if (i > 0) tagsCsv += ", ";
                    tagsCsv += post.getTags().get(i);
                }
            }
            
            model.addAttribute("post", post);
            model.addAttribute("tagsCsv", tagsCsv);
            model.addAttribute("currentUser", currentUser);
            return "edit_post";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load post for editing: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            return "error";
        }
    }

    @PostMapping("/posts/{id}")
    public String submitUpdatePost(@PathVariable Long id,
                               @ModelAttribute("post") PostDto post,
                               @RequestParam(value = "tagsCsv", required = false) String tagsCsv,
                               Model model,
                               HttpSession session) {
    UserDto currentUser = (UserDto) session.getAttribute("user");
    if (currentUser == null) {
        return "redirect:/auth/signin";
    }
    try {
        PostDto existingPost = postServiceClient.getPostById(id);
        if (!existingPost.getAuthor().equals(currentUser.getName()) && !"ADMIN".equals(currentUser.getRole())) {
            return "redirect:/posts/" + id;
        }

        post.setId(id);
        post.setCreatedAt(existingPost.getCreatedAt());
        post.setAuthor(existingPost.getAuthor());

        if (post.getExcerpt() == null || post.getExcerpt().trim().isEmpty()) {
            post.setExcerpt(existingPost.getExcerpt());
        }

        if (tagsCsv != null && !tagsCsv.trim().isEmpty()) {
            List<String> tags = new ArrayList<>();
            String[] parts = tagsCsv.split(",");
            for (String part : parts) {
                String trimmedTag = part.trim();
                if (!trimmedTag.isEmpty()) {
                    tags.add(trimmedTag);
                }
            }
            post.setTags(tags);
        } else {
            post.setTags(existingPost.getTags());
        }

        if (post.isPublished()) {
            post.setPublishedAt(LocalDateTime.now());
        }

        PostDto updated = postServiceClient.updatePost(id, post);
        return "redirect:/posts/" + updated.getId();
    } catch (Exception e) {
        e.printStackTrace();
        model.addAttribute("error", "Unable to update post: " + e.getMessage());
        model.addAttribute("currentUser", currentUser);
        return "error";
    }
}

    @PostMapping("/posts/{id}/delete")
    public String deletePost(@PathVariable Long id, Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        try {
            PostDto existingPost = postServiceClient.getPostById(id);
            if (!existingPost.getAuthor().equals(currentUser.getName()) && !"ADMIN".equals(currentUser.getRole())) {
                return "redirect:/posts/" + id;
            }
            postServiceClient.deletePost(id);
            return "redirect:/";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to delete post: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            return "error";
        }
    }

    @GetMapping("/users/profile")
    public String currentUserProfile(Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        
        try {
            List<PostDto> userPosts = postServiceClient.getPostsByAuthor(currentUser.getName());
            List<CommentDto> userComments = new ArrayList<>();
            model.addAttribute("user", currentUser);
            model.addAttribute("posts", userPosts);
            model.addAttribute("comments", userComments);
            model.addAttribute("currentUser", currentUser);
            return "user_profile";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load user profile: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            return "error";
        }
    }

    @GetMapping("/users/{username}/profile")
    public String viewUserProfile(@PathVariable String username, Model model, HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        
        try {
            UserDto profileUser = userServiceClient.getUserByUsername(username);
            if (profileUser == null) {
                model.addAttribute("error", "User not found");
                return "error";
            }
            
            List<PostDto> userPosts = postServiceClient.getPostsByAuthor(username);
            
            model.addAttribute("user", profileUser);
            model.addAttribute("posts", userPosts);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isOwnProfile", false);
            
            return "user_profile";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load user profile: " + e.getMessage());
            model.addAttribute("currentUser", currentUser);
            return "error";
        }
    }

    @PostMapping("/posts/{postId}/comments")
    public String createComment(@PathVariable Long postId,
                                @RequestParam String comment,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        try {
            UserDto currentUser = (UserDto) session.getAttribute("user");
            
            if (currentUser == null) {
                redirectAttributes.addFlashAttribute("error", "You must be logged in to comment.");
                return "redirect:/auth/signin";
            }

            CommentDto commentDto = new CommentDto();
            commentDto.setPostId(postId);
            commentDto.setName(currentUser.getName());
            commentDto.setEmail(currentUser.getEmail());
            commentDto.setComment(comment);

            commentServiceClient.createComment(commentDto);

            redirectAttributes.addFlashAttribute("message", "Comment added successfully!");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Unable to add comment: " + e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    @GetMapping("/comments/{commentId}/edit")
    public String editCommentForm(@PathVariable Long commentId,
                                Model model,
                                HttpSession session) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }
        
        try {
            CommentDto comment = commentServiceClient.getCommentById(commentId);
            
            if (!comment.getName().equals(currentUser.getName()) &&
                !"ADMIN".equals(currentUser.getRole())) {
                return "redirect:/posts/" + comment.getPostId();
            }
            
            model.addAttribute("comment", comment);
            model.addAttribute("currentUser", currentUser);
            return "edit_comment";
        } catch (Exception e) {
            model.addAttribute("error", "Unable to load comment for editing: " + e.getMessage());
            return "error";
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/edit")
    public String updateComment(@PathVariable Long postId,
                                @PathVariable Long commentId,
                                @RequestParam String comment,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }

        try {
            CommentDto existingComment = commentServiceClient.getCommentById(commentId);

            if (!existingComment.getName().equals(currentUser.getName()) &&
                    !"ADMIN".equals(currentUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "You can only edit your own comments.");
                return "redirect:/posts/" + postId;
            }

            existingComment.setComment(comment);

            commentServiceClient.updateComment(commentId, existingComment);

            redirectAttributes.addFlashAttribute("message", "Comment updated successfully!");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("error", "Unable to update comment: " + e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }

    @PostMapping("/posts/{postId}/comments/{commentId}/delete")
    public String deleteComment(@PathVariable Long postId,
                                @PathVariable Long commentId,
                                HttpSession session,
                                RedirectAttributes redirectAttributes) {
        UserDto currentUser = (UserDto) session.getAttribute("user");
        if (currentUser == null) {
            return "redirect:/auth/signin";
        }

        try {
            CommentDto existingComment = commentServiceClient.getCommentById(commentId);

            if (!existingComment.getName().equals(currentUser.getName()) &&
                    !"ADMIN".equals(currentUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "You can only delete your own comments.");
                return "redirect:/posts/" + postId;
            }

            commentServiceClient.deleteComment(commentId);

            redirectAttributes.addFlashAttribute("message", "Comment deleted successfully!");
            return "redirect:/posts/" + postId;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Unable to delete comment: " + e.getMessage());
            return "redirect:/posts/" + postId;
        }
    }
}
