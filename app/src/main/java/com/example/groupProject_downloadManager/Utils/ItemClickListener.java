package com.example.groupProject_downloadManager.Utils;

import com.example.groupProject_downloadManager.Model.DownloadModel;

public interface ItemClickListener {
    void onCLickItem(String file_path);
    void onShareClick(DownloadModel downloadModel);
}
