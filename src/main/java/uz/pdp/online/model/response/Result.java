package uz.pdp.online.model.response;

import com.google.gson.annotations.SerializedName;

public class Result{

	@SerializedName("file_path")
	private String filePath;

	@SerializedName("file_unique_id")
	private String fileUniqueId;

	@SerializedName("file_id")
	private String fileId;

	@SerializedName("file_size")
	private int fileSize;

	public String getFilePath(){
		return filePath;
	}

	public String getFileUniqueId(){
		return fileUniqueId;
	}

	public String getFileId(){
		return fileId;
	}

	public int getFileSize(){
		return fileSize;
	}
}