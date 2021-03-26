package com.semicolon.learnera;

import com.bumptech.glide.annotation.Excludes;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpLibraryGlideModule;

@GlideModule
@Excludes(OkHttpLibraryGlideModule.class)
public class AppGlideModule extends com.bumptech.glide.module.AppGlideModule {


}
