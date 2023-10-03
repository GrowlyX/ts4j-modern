package com.xafero.ts4j;

import org.apache.commons.io.FileUtils;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author GrowlyX
 * @since 10/1/2023
 */
public enum AndroidReflectionUtilities {
    ;

    private static Class<?> APPLICATION_CLASS;
    private static Class<?> ASSET_MANAGER_CLASS;

    private static Method GET_DEF_CONTEXT_METHOD;
    private static Method APPLICATION_GET_ASSET_MANAGER_METHOD;

    private static Method ASSET_MANAGER_LIST;
    private static Method ASSET_MANAGER_OPEN;

    private static Object DEF_CONTEXT;
    private static Object ASSET_MANAGER;

    @Nullable
    public static InputStream open(@NotNull final String path) throws IOException {
        if (ASSET_MANAGER_OPEN == null) {
            return FileUtils.openInputStream(new File(path));
        }

        try {
            return (InputStream) ASSET_MANAGER_OPEN.invoke(ASSET_MANAGER, path);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    @Nullable
    public static String[] list(@NotNull final String path) {
        if (ASSET_MANAGER_LIST == null) {
            return new File(path).list();
        }

        try {
            return (String[]) ASSET_MANAGER_LIST.invoke(ASSET_MANAGER, path);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    static {
        try {
            APPLICATION_CLASS = Class.forName("android.app.Application");
            ASSET_MANAGER_CLASS = Class.forName("android.content.res.AssetManager");

            GET_DEF_CONTEXT_METHOD = AppUtil.class.getMethod("getDefContext");
            APPLICATION_GET_ASSET_MANAGER_METHOD = APPLICATION_CLASS.getMethod("getAssets");

            DEF_CONTEXT = GET_DEF_CONTEXT_METHOD.invoke(AppUtil.getInstance());
            ASSET_MANAGER = APPLICATION_GET_ASSET_MANAGER_METHOD.invoke(DEF_CONTEXT);

            ASSET_MANAGER_LIST = ASSET_MANAGER_CLASS.getMethod("list", String.class);
            ASSET_MANAGER_OPEN = ASSET_MANAGER_CLASS.getMethod("open", String.class);
        } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException ignored) {

        }
    }
}
