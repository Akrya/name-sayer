package application;

import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeViewModel {
    //adapter class that works with NamesModel objects to get their name and files displayed

    public void populateTree(TreeView<String> tree, int identifier, NamesListModel namesListModel){
        TreeItem<String> root = new TreeItem<>("Names");
        char[] alphabet = "abcdefghijklmnopqrstuvwxyz".toUpperCase().toCharArray();
        TreeItem<String>[] alphabetHeadings = new TreeItem[27];
        for (int i=0;i<26;i++){
            alphabetHeadings[i]= makeBranch(root, Character.toString(alphabet[i]));
        }
        alphabetHeadings[26]=makeBranch(root,"Other");

        //make branch for recordings
        for (int i=0;i<26;i++){
            ArrayList<String> names = new ArrayList<>();
            names.addAll(namesListModel.getNamesForLetter(alphabet[i], identifier));
            for (String s : names){
                TreeItem<String> heading = makeBranch(alphabetHeadings[i], s);
                NamesModel nameModel = namesListModel.getName(s);
                Map<String, Integer> recordingsMap = nameModel.getRecordings();
                List<String> recordings = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : recordingsMap.entrySet()){
                    if (entry.getValue() == identifier){
                        recordings.add(entry.getKey());
                    }
                }
                for (String recording : recordings){
                    makeBranch(heading, recording.substring(recording.indexOf('_')+1));
                }
            }
        }

        root.setExpanded(true);
        tree.setRoot(root);

    }

    private TreeItem<String> makeBranch(TreeItem<String> parent, String title){
        TreeItem<String> branch = new TreeItem<>(title);
        parent.getChildren().add(branch);
        if (calcHeight(branch) != 3){
            branch.setExpanded(true);
        } else {
            branch.setExpanded(false);
        }
        return branch;
    }

    public int calcHeight(TreeItem<String> selection){ //recursive function to calculate height of the selected item in tree
        if (selection.getParent() == null){
            return 1;
        } else {
            return calcHeight(selection.getParent())+1;
        }

    }


}
