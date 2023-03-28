package io.extremum.watch.aop;

/**
 * @author rpuch
 */
class WatchCaptureContext {
    private static final ThreadLocal<Boolean> patching = ThreadLocal.withInitial(() -> false);

    static void enterPatching() {
        patching.set(true);
    }

    static void exitPatching() {
        patching.set(false);
    }

    static boolean isPatching() {
        return patching.get();
    }
}
