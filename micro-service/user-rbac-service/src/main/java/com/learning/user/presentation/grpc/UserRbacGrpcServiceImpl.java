package com.learning.user.presentation.grpc;

import com.learning.user.application.dto.UserProfileDto;
import com.learning.user.application.exception.UserNotFoundException;
import com.learning.user.application.service.UserService;
import com.learning.user.grpc.v1.GetUserProfileRequest;
import com.learning.user.grpc.v1.GetUserProfileResponse;
import com.learning.user.grpc.v1.UserRbacGrpcServiceGrpc;
import com.learning.user.grpc.v1.ValidatePermissionsRequest;
import com.learning.user.grpc.v1.ValidatePermissionsResponse;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import net.devh.boot.grpc.server.service.GrpcService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@GrpcService
public class UserRbacGrpcServiceImpl extends UserRbacGrpcServiceGrpc.UserRbacGrpcServiceImplBase {

    private static final Logger log = LoggerFactory.getLogger(UserRbacGrpcServiceImpl.class);
    private final UserService userService;

    public UserRbacGrpcServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void getUserProfile(GetUserProfileRequest request, StreamObserver<GetUserProfileResponse> responseObserver) {
        try {
            UserProfileDto profile = userService.getUserProfileById(request.getUserId());
            GetUserProfileResponse response = GetUserProfileResponse.newBuilder()
                    .setUserId(profile.id())
                    .setEmail(profile.email())
                    .setFullName(profile.fullName())
                    .addAllRoles(profile.roles())
                    .addAllPermissions(profile.permissions())
                    .setStatus(profile.status())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found for gRPC profile query: {}", request.getUserId());
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error retrieving user profile for ID: {}", request.getUserId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal error retrieving profile").asRuntimeException()
            );
        }
    }

    @Override
    public void validatePermissions(ValidatePermissionsRequest request, StreamObserver<ValidatePermissionsResponse> responseObserver) {
        try {
            boolean isAuthorized = userService.validateUserPermission(request.getUserId(), request.getRequiredPermission());
            ValidatePermissionsResponse response = ValidatePermissionsResponse.newBuilder()
                    .setIsAuthorized(isAuthorized)
                    .setUserId(request.getUserId())
                    .build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (UserNotFoundException e) {
            log.warn("User not found for gRPC permission check: {}", request.getUserId());
            responseObserver.onError(
                    Status.NOT_FOUND.withDescription(e.getMessage()).asRuntimeException()
            );
        } catch (Exception e) {
            log.error("Error validating permissions for user ID: {}", request.getUserId(), e);
            responseObserver.onError(
                    Status.INTERNAL.withDescription("Internal error validating permissions").asRuntimeException()
            );
        }
    }
}
