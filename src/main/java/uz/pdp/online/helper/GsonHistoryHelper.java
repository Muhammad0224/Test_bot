package uz.pdp.online.helper;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import uz.pdp.online.model.history.History;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GsonHistoryHelper {
    Gson gson = new GsonBuilder().setPrettyPrinting().create();

    public List<History> converter(File file) {
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            return new ArrayList<>(Arrays.asList(gson.fromJson(bufferedReader, History[].class)));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
