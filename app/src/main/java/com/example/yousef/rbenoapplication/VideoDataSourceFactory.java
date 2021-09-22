package com.example.yousef.rbenoapplication;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.util.Log;
import com.google.android.exoplayer2.util.Util;

import java.io.File;

public class VideoDataSourceFactory implements DataSource.Factory {
    private final Context context;
    private final DefaultDataSourceFactory defaultDatasourceFactory;
    private static final long maxFileSize = 5 * 1024 * 1024, maxCacheSize = 15 * 1024 * 1024;


    public VideoDataSourceFactory(Context context) {
        super();
        this.context = context;

        String userAgent = Util.getUserAgent(context, context.getString(R.string.app_name));

        DefaultBandwidthMeter bandwidthMeter = new DefaultBandwidthMeter.Builder(context).build();

        defaultDatasourceFactory = new DefaultDataSourceFactory(this.context,
                bandwidthMeter,
                new DefaultHttpDataSourceFactory(userAgent, bandwidthMeter));
    }

    @Override
    public DataSource createDataSource() {

//      final Gson gson = new Gson();
        final SharedPreferences mPrefs =
                context.getSharedPreferences("rbeno",
                        Context.MODE_PRIVATE);

//      SimpleCache simpleCache;
//
////      if(mPrefs.contains("simpleCache")){
////        simpleCache = gson.fromJson(mPrefs.getString("simpleCache", ""),
////                SimpleCache.class);
////      }else{
//        simpleCache =
//                new SimpleCache(
//                        new File(context.getCacheDir(), "media"),
//                        new LeastRecentlyUsedCacheEvictor(maxCacheSize),
//                        new ExoDatabaseProvider(context));

//        Log.d("exoPlayerPlayback",
//                String.valueOf("simpleCache exists: "+simpleCache == null));

//        mPrefs.edit().putString("simpleCache",gson.toJson(simpleCache)).apply();
//      }


        TransferListener transferListener = new TransferListener() {
            @Override
            public void onTransferInitializing(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                Log.d("exoPlayerPlayback", "transfer initializing"
                        + " , is from network: " + isNetwork);
            }

            @Override
            public void onTransferStart(DataSource source, DataSpec dataSpec, boolean isNetwork) {

                Log.d("exoPlayerPlayback", "transfer started"
                        + " , is from network: " + isNetwork);

            }

            @Override
            public void onBytesTransferred(DataSource source, DataSpec dataSpec,
                                           boolean isNetwork, int bytesTransferred) {

                Log.d("exoPlayerPlayback", "progress: " + bytesTransferred
                        + " , is from network: " + isNetwork);

            }


            @Override
            public void onTransferEnd(DataSource source, DataSpec dataSpec, boolean isNetwork) {
                Log.d("exoPlayerPlayback", "transfer ended"
                        + " , is from network: " + isNetwork);
            }
        };


        CacheDataSource cacheDataSource = new CacheDataSource(
                VideoCache.getInstance(context),
                defaultDatasourceFactory.createDataSource(),
                new FileDataSource(),
                new CacheDataSink(VideoCache.getInstance(context), maxFileSize),
                CacheDataSource.FLAG_BLOCK_ON_CACHE |
                        CacheDataSource.FLAG_IGNORE_CACHE_ON_ERROR, null);

        cacheDataSource.addTransferListener(transferListener);

        return cacheDataSource;
    }

    public static void clearVideoCache(Context context) {

        Log.d("exoPlayerPlayback", "clearing video cache");

        try {
            File dir = new File(context.getCacheDir(), "media");
            deleteDir(dir);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static boolean deleteDir(File dir) {
        Log.d("exoPlayerPlayback", "trying to delete dir");
        if (dir != null && dir.isDirectory()) {
            Log.d("exoPlayerPlayback", "dir != null && dir.isDirectory()");
            final String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    Log.d("exoPlayerPlayback", "found child: " + child);
                    boolean success = deleteDir(new File(dir, child));
                    if (!success) {
                        Log.d("exoPlayerPlayback", "failed to deleted: " + child);
                        return false;
                    }
                }
            }
            return dir.delete();
        } else if (dir != null && dir.isFile()) {
            Log.d("exoPlayerPlayback", "dir!= null && dir.isFile()");
            return dir.delete();
        } else {
            return false;
        }
    }
}
