package de.dennisguse.opentracks.content;

import android.content.Context;
import android.content.UriMatcher;
import android.content.pm.ProviderInfo;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

import de.dennisguse.opentracks.android.IContentResolver;
import de.dennisguse.opentracks.io.file.TrackFileFormat;
import de.dennisguse.opentracks.io.file.exporter.FileTrackExporter;
import de.dennisguse.opentracks.io.file.exporter.TrackWriter;

/**
 * A content provider that mimics the behavior of {@link androidx.core.content.FileProvider}, which shares virtual (non-existing) KML-files.
 * The actual content of the virtual files is generated by using the functionality defined in {@link CustomContentProvider}.
 *
 * Moreover, it manages access to OpenTrack's database via {@link CustomContentProvider}.
 *
 * Explanation:
 * Although a request is handled by a {@link android.content.ContentProvider} (with temporarily granted permission), Android's security infrastructure prevents forwarding queries to non-exported {@link android.content.ContentProvider}.
 * Thus, if {@link ShareContentProvider} and {@link CustomContentProvider} would be two different instances, the data would not be accessible to external apps.
 * While handling a request {@link ShareContentProvider} could `grantPermissions()` to the calling app for {@link CustomContentProvider}'s URI.
 * However, while handling the request this would allow the calling app to actually contact {@link CustomContentProvider} directly and get access to stored data that should remain private.
 *
 */
public class ShareContentProvider extends CustomContentProvider implements IContentResolver {

    private static final String[] COLUMNS = {OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE};

    public static final String TAG = ShareContentProvider.class.getCanonicalName();

    public static String MIME = "application/kml+xml";
    private static final int URI_KML = 1;
    private final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    public static Uri createURI(long[] trackIds) {
        if (trackIds.length == 0) {
            throw new UnsupportedOperationException();
        }

        StringBuilder builder = new StringBuilder();
        for (long trackId : trackIds) {
            builder.append(trackId).append(",");
        }
        builder.deleteCharAt(builder.lastIndexOf(","));

        return Uri.parse(ContentProviderUtils.CONTENT_BASE_URI + "/" + TracksColumns.TABLE_NAME + "/kml/" + builder + ".kml");
    }

    @Override
    public boolean onCreate() {
        uriMatcher.addURI(ContentProviderUtils.AUTHORITY_PACKAGE, TracksColumns.TABLE_NAME + "/kml/*", URI_KML);
        return super.onCreate();
    }

    /**
     * Do not allow to be exported via AndroidManifest.
     * Check that caller has permissions to access {@link CustomContentProvider}.
     */
    @Override
    public void attachInfo(@NonNull Context context, @NonNull ProviderInfo info) {
        super.attachInfo(context, info);

        // Sanity check our security
        if (info.exported) {
            throw new UnsupportedOperationException("Provider must not be exported");
        }

        if (!info.grantUriPermissions) {
            throw new SecurityException("Provider must grant uri permissions");
        }
    }

    private static long[] parseURI(Uri uri) {
        String lastPathSegment = uri.getLastPathSegment();
        if (lastPathSegment == null) {
            return new long[]{};
        }

        String[] lastPathSegmentSplit = lastPathSegment.replace(".kml", "").split(",");
        long[] trackIds = new long[lastPathSegmentSplit.length];
        for (int i = 0; i < trackIds.length; i++) {
            trackIds[i] = Long.valueOf(lastPathSegmentSplit[i]);
        }
        return trackIds;
    }

    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        if (uriMatcher.match(uri) != URI_KML) {
            return super.query(uri, projection, selection, selectionArgs, sortOrder);
        }

        // ContentProvider has already checked granted permissions
        if (projection == null) {
            projection = COLUMNS;
        }

        String[] cols = new String[projection.length];
        Object[] values = new Object[projection.length];
        int i = 0;
        for (String col : projection) {
            if (OpenableColumns.DISPLAY_NAME.equals(col)) {
                cols[i] = OpenableColumns.DISPLAY_NAME;
                values[i++] = uri.getLastPathSegment();
            } else if (OpenableColumns.SIZE.equals(col)) {
                cols[i] = OpenableColumns.SIZE;
                values[i++] = -1;
            }
        }

        cols = Arrays.copyOf(cols, i);
        values = Arrays.copyOf(values, i);

        final MatrixCursor cursor = new MatrixCursor(cols, 1);
        cursor.addRow(values);
        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        if (uriMatcher.match(uri) == URI_KML) {
            return MIME;
        }
        return super.getType(uri);
    }

    @Nullable
    @Override
    public ParcelFileDescriptor openFile(@NonNull Uri uri, @NonNull String mode) throws FileNotFoundException {
        ContentProviderUtils contentProviderUtils = new ContentProviderUtilsImpl(this);

        long[] trackIds = parseURI(uri);
        final Track[] tracks = new Track[trackIds.length];
        for (int i = 0; i < trackIds.length; i++) {
            tracks[i] = contentProviderUtils.getTrack(trackIds[i]);
        }

        TrackWriter kmlTrackWriter = TrackFileFormat.KML.newTrackWriter(getContext(), false);
        final FileTrackExporter fileTrackExporter = new FileTrackExporter(contentProviderUtils, tracks, kmlTrackWriter, null);

        PipeDataWriter pipeDataWriter = new PipeDataWriter<String>() {
            @Override
            public void writeDataToPipe(@NonNull ParcelFileDescriptor output, @NonNull Uri uri, @NonNull String mimeType, @Nullable Bundle opts, @Nullable String args) {
                try (FileOutputStream fileOutputStream = new FileOutputStream(output.getFileDescriptor())) {
                    fileTrackExporter.writeTrack(fileOutputStream);
                } catch (IOException e) {
                    Log.w(TAG, "Oops closing " + e);
                    Toast.makeText(getContext(), "", Toast.LENGTH_SHORT).show();
                }
            }
        };

        return openPipeHelper(uri, getType(uri), null, null, pipeDataWriter);
    }
}
