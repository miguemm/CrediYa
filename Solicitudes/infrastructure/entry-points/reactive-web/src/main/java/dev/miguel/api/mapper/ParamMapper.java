package dev.miguel.api.mapper;


public final class ParamMapper {

    public static Long toLongOrNull(String v) {
        try { return v == null ? null : Long.valueOf(v); } catch (Exception e) { return null; }
    }

    public static Integer toIntOrNull(String v) {
        try { return v == null ? null : Integer.valueOf(v); } catch (Exception e) { return null; }
    }

}
