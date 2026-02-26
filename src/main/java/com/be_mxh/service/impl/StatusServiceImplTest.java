package com.be_mxh.service.impl;

import com.be_mxh.dto.status.StatusResponse;
import com.be_mxh.entity.Status;
import com.be_mxh.entity.StatusImage;
import com.be_mxh.entity.User;
import com.be_mxh.repository.StatusImageRepository;
import com.be_mxh.repository.StatusRepository;
import com.be_mxh.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatusServiceImplTest {

    @Mock
    private StatusRepository statusRepository;

    @Mock
    private StatusImageRepository statusImageRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatusServiceImpl statusService;

    private User testUser;
    private Status testStatus;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
                .id(1L)
                .username("test_user")
                .email("test@example.com")
                .build();

        testStatus = Status.builder()
                .id(1L)
                .user(testUser)
                .content("Test status")
                .visibility(Status.Visibility.PUBLIC)
                .active(true)
                .createdAt(LocalDateTime.now())
                .build();
    }

    /**
     * ✅ TEST 1: Valid pagination parameters
     */
    @Test
    void testGetPublicStatusesByUser_ValidParams_Success() {
        // Arrange
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Status> statusPage = new PageImpl<>(List.of(testStatus));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(statusRepository.findPublicStatusesByUser(userId, Status.Visibility.PUBLIC, pageable))
                .thenReturn(statusPage);
        when(statusImageRepository.findByStatusIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of());

        // Act
        Page<StatusResponse> result = statusService.getPublicStatusesByUser(userId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("test_user", result.getContent().get(0).getUsername());
    }

    /**
     * ✅ TEST 2: Invalid page number (negative)
     */
    @Test
    void testGetPublicStatusesByUser_NegativePage_ThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statusService.getPublicStatusesByUser(1L, -1, 10)
        );
        assertTrue(exception.getMessage().contains("âm"));
    }

    /**
     * ✅ TEST 3: Invalid size (too large)
     */
    @Test
    void testGetPublicStatusesByUser_SizeTooLarge_ThrowException() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> statusService.getPublicStatusesByUser(1L, 0, 101)
        );
        assertTrue(exception.getMessage().contains("Size phải từ 1 đến 100"));
    }

    /**
     * ✅ TEST 4: User not found
     */
    @Test
    void testGetPublicStatusesByUser_UserNotFound_ThrowException() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        EntityNotFoundException exception = assertThrows(
                EntityNotFoundException.class,
                () -> statusService.getPublicStatusesByUser(999L, 0, 10)
        );
        assertTrue(exception.getMessage().contains("không tồn tại"));
    }

    /**
     * ✅ TEST 5: Empty result (no public statuses)
     */
    @Test
    void testGetPublicStatusesByUser_EmptyResult_Success() {
        // Arrange
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Status> emptyPage = new PageImpl<>(List.of());

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(statusRepository.findPublicStatusesByUser(userId, Status.Visibility.PUBLIC, pageable))
                .thenReturn(emptyPage);

        // Act
        Page<StatusResponse> result = statusService.getPublicStatusesByUser(userId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
    }

    /**
     * ✅ TEST 6: Pagination with images
     */
    @Test
    void testGetPublicStatusesByUser_WithImages_Success() {
        // Arrange
        long userId = 1L;
        Pageable pageable = PageRequest.of(0, 10);
        Page<Status> statusPage = new PageImpl<>(List.of(testStatus));

        StatusImage image = StatusImage.builder()
                .id(1L)
                .status(testStatus)
                .url("http://example.com/image.jpg")
                .sortOrder(1)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(statusRepository.findPublicStatusesByUser(userId, Status.Visibility.PUBLIC, pageable))
                .thenReturn(statusPage);
        when(statusImageRepository.findByStatusIdOrderBySortOrderAsc(1L))
                .thenReturn(List.of(image));

        // Act
        Page<StatusResponse> result = statusService.getPublicStatusesByUser(userId, 0, 10);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getContent().get(0).getImageUrls().size());
        assertEquals("http://example.com/image.jpg", result.getContent().get(0).getImageUrls().get(0));
    }
}