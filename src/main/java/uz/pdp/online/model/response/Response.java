package uz.pdp.online.model.response;

import com.google.gson.annotations.SerializedName;

public class Response{

	@SerializedName("result")
	private Result result;

	@SerializedName("ok")
	private boolean ok;

	public Result getResult(){
		return result;
	}

	public boolean isOk(){
		return ok;
	}
}