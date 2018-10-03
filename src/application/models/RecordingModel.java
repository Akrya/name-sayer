package application.models;

public class RecordingModel {

    private int _identifier; //identifier for recording, 0 if it is a database recording and 1 if it is personal

    private String _date;

    private String _time;

    private String _fileName;

    private String _name;

    private int _versionNum;

    private boolean _goodRating; //by default each recording has good rating unless stating otherwise by the rating button

    public RecordingModel(String fileName, String name, int identifier){
        _fileName = fileName;
        _name = name;
        _identifier = identifier;
        _goodRating = true;
        findDate();
        findVersion();
        findRating();
    }

    private void findRating() {

    }

    private void findDate(){
        if (_identifier == 0){
            _date = _fileName.substring(_fileName.indexOf('_')+1,_fileName.lastIndexOf('_'));
        } else {
            _date = _fileName.substring(_fileName.indexOf('_')+1,_fileName.indexOf('v')-1);
        }
        _time = _date.substring(_date.indexOf('_')+1);
        _date = _date.substring(0,_date.indexOf('_'));
    }

    private void findVersion(){
        if (_identifier == 0){
            _versionNum = 0;
        } else {
            _versionNum = Integer.parseInt(_fileName.substring(_fileName.indexOf('(')+1,_fileName.indexOf(')')));
        }
    }

    public String getFileName(){
        return _fileName;
    }

    public String getDate(){
        return _date;
    }

    public int getIdentifier(){
        return _identifier;
    }

    public void setRating(boolean rating){
        _goodRating = rating;
    }

    public String getRating(){
        if (_goodRating){
            return "Good";
        } else {
            return "Bad";
        }
    }

}
