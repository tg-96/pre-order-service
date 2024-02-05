package com.preOrderService.api.external;

import com.preOrderService.config.JWTUtil;
import com.preOrderService.dto.*;
import com.preOrderService.dto.request.RequestMemberDto;
import com.preOrderService.service.AwsS3Service;
import com.preOrderService.service.NewsFeedService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/api/newsFeed")
@RequiredArgsConstructor
public class ExternalNewsFeedController {
    private final NewsFeedService newsFeedService;
    private final AwsS3Service awsS3Service;
    private final JWTUtil jwtUtil;

    /**
     * 팔로우
     */
    @PostMapping("/follow")
    public ResponseEntity<Void> follow(@RequestBody RequestMemberDto request, @RequestHeader("Authorization") String token) {
        String parse_token = jwtUtil.parser(token);

        //유효기간 만료확인
        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        Long fromMemberId = jwtUtil.getUserId(parse_token);
        Long toMemberId = request.getMemberId();

        newsFeedService.changeFollow(token, fromMemberId, toMemberId);

        return ResponseEntity.ok().build();
    }

    /**
     * 피드 조회
     */
    @GetMapping
    public List<FeedsDto> getNewsFeeds(@RequestHeader("Authorization") String token) {
        String parse_token = jwtUtil.parser(token);
        //토큰 유효성 검증
        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }
        Long memberId = jwtUtil.getUserId(parse_token);

        //뉴스피드 조회
        List<FeedsDto> newsFeeds = newsFeedService.getFeeds(memberId);

        return newsFeeds;
    }


    /**
     * 게시글 작성
     */
    @PostMapping("/posts")
    public ResponseEntity<Void> writePost(@RequestPart(value = "postsDto") PostsDto postsDto, @RequestPart(value = "file") MultipartFile multipartFile, @RequestHeader("Authorization") String token) {
        String parse_token = jwtUtil.parser(token);

        //토큰 유효성 검증
        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        Long memberId = jwtUtil.getUserId(parse_token);

        //이미지 정보 s3에 업로드후, 프로필 이미지 추가
        String image = "";
        if (!multipartFile.isEmpty()) {
            image = awsS3Service.uploadFile(multipartFile);
        }

        newsFeedService.writePost(token, memberId, postsDto,image);

        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 작성
     */
    @PostMapping("/comments/{postId}")
    public ResponseEntity<Void> writeComments(@RequestHeader("Authorization") String token,
                                              @PathVariable("postId") Long postId,
                                              @RequestBody CommentsDto commentsDto) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        Long memberId = jwtUtil.getUserId(parse_token);

        newsFeedService.writeComments(token, memberId, postId, commentsDto);

        return ResponseEntity.ok().build();
    }

    /**
     * 댓글 조회
     */
    @GetMapping("/comments/{postId}")
    public List<CommentsResponseDto> findComments(@RequestHeader("Authorization") String token,
                                                  @PathVariable("postId") Long postId) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        return newsFeedService.findCommentsByPost(postId);
    }

    /**
     * 게시글 좋아요
     */
    @PostMapping("/posts/like/{postId}")
    public ResponseEntity<Void> postLike(@RequestHeader("Authorization") String token,
                                         @PathVariable("postId") Long postId) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        Long memberId = jwtUtil.getUserId(parse_token);

        newsFeedService.postLike(token, memberId, postId);
        return ResponseEntity.ok().build();
    }

    /**
     * 게시글별 좋아요 조회
     */
    @GetMapping("/posts/like/{postId}")
    public List<PostLikesDto> findPostLikes(@RequestHeader("Authorization") String token,
                                            @PathVariable("postId") Long postId) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        return newsFeedService.findPostLike(postId);
    }

    /**
     * 댓글 좋아요
     */
    @PostMapping("/comments/like/{commentId}")
    public ResponseEntity<Void> commentLike(@RequestHeader("Authorization") String token,
                                            @PathVariable("commentId") Long commentId) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        Long memberId = jwtUtil.getUserId(parse_token);

        newsFeedService.commentLike(token, commentId, memberId);
        return ResponseEntity.ok().build();
    }

    /**
     * 댓글별 좋아요 조회
     */
    @GetMapping("/comments/like/{commentId}")
    public List<CommentLikesDto> findCommentLikes(@RequestHeader("Authorization") String token,
                                                  @PathVariable("commentId") Long commentId) {
        String parse_token = jwtUtil.parser(token);

        if (jwtUtil.isExpired(parse_token)) {
            throw new RuntimeException("토큰이 유효하지 않습니다.");
        }

        return newsFeedService.findCommentLike(commentId);
    }

}
