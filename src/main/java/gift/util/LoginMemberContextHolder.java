package gift.util;

public final class LoginMemberContextHolder {

    private static final ThreadLocal<Long> currentUserId = new ThreadLocal<>();

    public static void set(Long userId) {
        currentUserId.set(userId);
    }

    public static Long get() {
        return currentUserId.get();
    }

    public static void clear() {
        currentUserId.remove();
    }

}
