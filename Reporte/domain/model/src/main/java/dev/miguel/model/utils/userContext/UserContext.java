package dev.miguel.model.utils.userContext;

import java.util.List;

public record UserContext(String id, String email, List<String> roles) {
}
