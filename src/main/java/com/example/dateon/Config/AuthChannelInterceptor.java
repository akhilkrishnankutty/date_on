package com.example.dateon.Config;

import com.example.dateon.Service.JwtService;
import com.example.dateon.Service.MyUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.lang.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;

@Component
public class AuthChannelInterceptor implements ChannelInterceptor, ApplicationContextAware {

    private JwtService jwtService;
    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    private JwtService getJwtService() {
        if (jwtService == null) {
            jwtService = applicationContext.getBean(JwtService.class);
        }
        return jwtService;
    }

    private MyUserDetailsService getUserDetailsService() {
        return applicationContext.getBean(MyUserDetailsService.class);
    }

    @Override
    public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            List<String> authorization = accessor.getNativeHeader("Authorization");
            if (authorization == null || authorization.isEmpty() || !authorization.get(0).startsWith("Bearer ")) {
                throw new AccessDeniedException("Unauthorized: Missing or invalid Authorization header");
            }

            String token = authorization.get(0).substring(7);
            String username = getJwtService().extractUserName(token);

            if (username == null) {
                throw new AccessDeniedException("Unauthorized: Invalid token");
            }

            UserDetails userDetails = getUserDetailsService().loadUserByUsername(username);
            if (!getJwtService().validateToken(token, userDetails)) {
                throw new AccessDeniedException("Unauthorized: Invalid or expired token");
            }

            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    userDetails, null, userDetails.getAuthorities());
            accessor.setUser(auth);
        }
        return message;
    }
}
