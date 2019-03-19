//排序用
import java.util.*;
import java.lang.*;
import java.io.*;


class MaxPropComparator implements Comparator<Answer>
{
	int me;
	//如果是電腦，由大排小
	//如果是對方，由小排大
	public int compare(Answer a,Answer b)
	{
		return (b.GetPrice()-a.GetPrice())*me;

	}
}