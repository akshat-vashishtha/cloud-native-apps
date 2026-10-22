package com.learning.chat.infrastructure.interceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.security.Principal;

@Component
public class StompDestinationAclInterceptor implements ChannelInterceptor {

    private static final Logger log = LoggerFactory.getLogger(StompDestinationAclInterceptor.class);

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);
        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        if (StompCommand.SUBSCRIBE.equals(accessor.getCommand())) {
            String destination = accessor.getDestination();
            Principal user = accessor.getUser();
            String principalName = user != null ? user.getName() : null;

            if (destination != null && destination.startsWith("/user/") && !destination.startsWith("/user/queue/")) {
                String expectedPrefix = "/user/" + principalName + "/";
                if (principalName == null || !destination.startsWith(expectedPrefix)) {
                    log.warn("STOMP ACL violation: principal [{}] attempted to subscribe to [{}]", principalName, destination);
                    throw new AccessDeniedException("Forbidden subscription to another user's private destination");
                }
            }
        }
        return message;
    }
}
