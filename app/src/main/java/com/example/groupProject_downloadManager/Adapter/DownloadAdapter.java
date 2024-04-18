package com.example.groupProject_downloadManager.Adapter;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.groupProject_downloadManager.Model.DownloadModel;
import com.example.groupProject_downloadManager.R;
import com.example.groupProject_downloadManager.Utils.DownloadDBHelper;
import com.example.groupProject_downloadManager.Utils.ItemClickListener;
import com.example.groupProject_downloadManager.Utils.UpdateTitle;

import java.io.File;
import java.util.List;

public class DownloadAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    Context context;
    public List<DownloadModel> downloadModels;
    ItemClickListener clickListener;
    private DownloadDBHelper myDB;
    private FragmentManager fragmentManager;

    private String path;

    public DownloadAdapter(Context context, List<DownloadModel> downloadModels,
                           ItemClickListener itemClickListener,
                           DownloadDBHelper myDB,
                           FragmentManager fragmentManager) {
        this.context = context;
        this.downloadModels = downloadModels;
        this.clickListener = itemClickListener;
        this.myDB = myDB;
        this.fragmentManager = fragmentManager;
    }

    public class DownloadViewHolder extends RecyclerView.ViewHolder {
        TextView file_title;
        TextView file_size;
        ProgressBar file_progress;
        Button pause_resume, sharefile;
        TextView file_status;
        RelativeLayout main_rel;

        public DownloadViewHolder(@NonNull View itemView) {
            super(itemView);
            file_title = itemView.findViewById(R.id.file_title);
            file_size = itemView.findViewById(R.id.file_size);
            file_status = itemView.findViewById(R.id.file_status);
            file_progress = itemView.findViewById(R.id.file_progress);
            pause_resume = itemView.findViewById(R.id.pause_resume);
            main_rel = itemView.findViewById(R.id.main_rel);
            //sharefile = itemView.findViewById(R.id.sharefile);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder vh;
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_download_row, parent, false);
        vh = new DownloadViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, final int position) {
        final DownloadModel downloadModel = downloadModels.get(position);
        final DownloadViewHolder downloadViewHolder = (DownloadViewHolder) holder;

        downloadViewHolder.file_title.setText(downloadModel.getTitle());
        downloadViewHolder.file_status.setText(downloadModel.getStatus());
        downloadViewHolder.file_progress.setProgress(Integer.parseInt(downloadModel.getProgress()));
        downloadViewHolder.file_size.setText("Downloaded : " + downloadModel.getFile_size());

        if (downloadModel.isIs_paused()) {
            downloadViewHolder.pause_resume.setText("RESUME");
        } else {
            downloadViewHolder.pause_resume.setText("PAUSE");
        }

        if (downloadModel.getStatus().equalsIgnoreCase("RESUME")) {
            downloadViewHolder.file_status.setText("Running");
        }

        downloadViewHolder.pause_resume.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (downloadModel.isIs_paused()) {
                    downloadModel.setIs_paused(false);
                    downloadViewHolder.pause_resume.setText("PAUSE");
                    downloadModel.setStatus("RESUME");
                    downloadViewHolder.file_status.setText("Running");
                    if (!resumeDownload(downloadModel)) {
                        Toast.makeText(context, "Failed to Resume", Toast.LENGTH_SHORT).show();
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                } else {
                    downloadModel.setIs_paused(true);
                    downloadViewHolder.pause_resume.setText("RESUME");
                    downloadModel.setStatus("PAUSE");
                    downloadViewHolder.file_status.setText("PAUSE");
                    if (!pauseDownload(downloadModel)) {
                        Toast.makeText(context, "Failed to Pause", Toast.LENGTH_SHORT).show();
                    }
                    notifyItemChanged(holder.getAdapterPosition());
                }
            }
        });
        downloadViewHolder.main_rel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onCLickItem(downloadModel.getFile_path());
            }
        });

        /*downloadViewHolder.sharefile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clickListener.onShareClick(downloadModel);
            }
        });*/
    }

    private boolean pauseDownload(DownloadModel downloadModel) {
        int updatedRow = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", 1);

        try {
            updatedRow = context.getContentResolver().update(Uri.parse("content://downloads/my_downloads"), contentValues, "title=?", new String[]{downloadModel.getTitle()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0 < updatedRow;
    }

    private boolean resumeDownload(DownloadModel downloadModel) {
        int updatedRow = 0;
        ContentValues contentValues = new ContentValues();
        contentValues.put("control", 0);

        try {
            updatedRow = context.getContentResolver().update(Uri.parse("content://downloads/my_downloads"), contentValues, "title=?", new String[]{downloadModel.getTitle()});
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0 < updatedRow;
    }

    @Override
    public int getItemCount() {
        return downloadModels.size();
    }

    public void changeItem(long downloadid) {
        int i = 0;
        for (DownloadModel downloadModel : downloadModels) {
            if (downloadid == downloadModel.getDownloadId()) {
                notifyItemChanged(i);
            }
            i++;
        }
    }

    public boolean ChangeItemWithStatus(String message, long downloadid) {
        boolean comp = false;
        int i = 0;
        for (DownloadModel downloadModel : downloadModels) {
            if (downloadid == downloadModel.getDownloadId()) {
                downloadModels.get(i).setStatus(message);
                notifyItemChanged(i);
                comp = true;
            }
            i++;
        }
        return comp;
    }

    public void setChangeItemFilePath(String path, long id) {
        int i = 0;
        for (DownloadModel downloadModel : downloadModels) {
            if (id == downloadModel.getDownloadId()) {
                downloadModels.get(i).setFile_path(path);
                notifyItemChanged(i);
            }
            i++;
        }
    }

    public Context getContext(){
        return context;
    }

    public void deleteDownload(int position){
        DownloadModel item = downloadModels.get(position);
        path = item.getFile_path();

        // Delete files from Files and Device Explorer (storage/emulated/0/Downloads and storage/self/primary/Downloads)
        deleteInFiles_DeviceExplorer(path);

        // Delete files from SQLite database
        myDB.deleteDownload(item.getDownloadId());

        // Delete files from DownloadModel list
        downloadModels.remove(position);
        notifyItemRemoved(position);
    }

    private void deleteInFiles_DeviceExplorer(String filePath) {
        // Remove the scheme (file://) from the file path
        String cleanedFilePath = Uri.parse(filePath).getPath();
        File file = new File(cleanedFilePath);
        boolean deleted = file.delete();

        if (!deleted) {
            // Handle the case where the file couldn't be deleted
            Log.e("Delete Error", "Failed to delete file: " + cleanedFilePath);
            return;
        }
        notifyDataSetChanged();
    }

    // Update downloaded file Title
    public void updateDownloadTitle(int position){
        final DownloadModel item = downloadModels.get(position);

        Bundle bundle = new Bundle();
        bundle.putLong("download_id", item.getDownloadId());
        bundle.putString("titleIncludeFileType", item.getTitle());
        bundle.putString("filePath", item.getFile_path());

        UpdateTitle updateTitle = new UpdateTitle();
        updateTitle.setOnTitleUpdateListener(new UpdateTitle.OnTitleUpdateListener() {
            @Override
            public void onTitleUpdated(String updatedTitle) {
                String oldFilePath = item.getFile_path();
                String newFilePath = "file:///storage/emulated/0/Download/";

                updateFilenameInFiles_DeviceExplorer(oldFilePath, updatedTitle);
                // Update the item's title and file path in the DownloadModel list
                item.setTitle(updatedTitle);

                item.setFile_path(newFilePath + updatedTitle);
                path = newFilePath + updatedTitle;

                notifyItemChanged(position);
            }
        });
        updateTitle.setArguments(bundle);
        updateTitle.show(fragmentManager, updateTitle.getTag());
        notifyItemChanged(position);
    }

    private void updateFilenameInFiles_DeviceExplorer(String oldFilePath, String newFileName) {
        // Remove the scheme (file://) from the old file path
        String cleanedFilePath = Uri.parse(oldFilePath).getPath();
        File oldFile = new File(cleanedFilePath);
        String parentDirectory = oldFile.getParent();

        // Create a new File object with the new file name and the parent directory
        File newFile = new File(parentDirectory, newFileName);
        boolean renamed = oldFile.renameTo(newFile);

        if (!renamed) {
            // Handle the case where the file couldn't be renamed
            Log.e("Rename Error", "Failed to rename file: " + oldFilePath);
            return;
        }
    }
}
