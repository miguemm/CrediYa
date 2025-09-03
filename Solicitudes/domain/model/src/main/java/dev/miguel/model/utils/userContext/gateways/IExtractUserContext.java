package dev.miguel.model.utils.userContext.gateways;

import dev.miguel.model.utils.userContext.UserContext;

public interface IExtractUserContext {
    UserContext toUserContext(Object p);
}
