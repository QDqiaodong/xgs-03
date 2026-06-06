package com.plantcare.service;

import com.plantcare.entity.Post;
import com.plantcare.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    public Page<Post> getPostsByType(String postType, Pageable pageable) {
        return postRepository.findByPostTypeOrderByCreatedAtDesc(postType, pageable);
    }

    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    public Optional<Post> getPostById(Long id) {
        Optional<Post> postOpt = postRepository.findById(id);
        postOpt.ifPresent(post -> {
            post.setViewCount(post.getViewCount() + 1);
            postRepository.save(post);
        });
        return postOpt;
    }

    public Post createPost(Post post) {
        return postRepository.save(post);
    }

    public Post updatePost(Long id, Post post) {
        post.setId(id);
        return postRepository.save(post);
    }

    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    public Post incrementCommentCount(Long postId) {
        Optional<Post> postOpt = postRepository.findById(postId);
        if (postOpt.isPresent()) {
            Post post = postOpt.get();
            post.setCommentCount(post.getCommentCount() + 1);
            return postRepository.save(post);
        }
        return null;
    }
}
