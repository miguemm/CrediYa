package dev.miguel.model.userContext.gateways;

import dev.miguel.model.userContext.UserContext;

public interface IExtractUserContext {
    UserContext toUserContext(Object p);
}
