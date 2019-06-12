package onedoller.gesture.recognizer;


public class Result extends Object
{
	public String Name;
	public double Score;
	public int Index;

	public Result(String name, double score, int index)
	{
		this.Name = name;
		this.Score = score;
		this.Index = index;
	}
}
