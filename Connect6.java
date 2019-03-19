import java.util.*;
import java.lang.*;
import java.io.*;

public class Connect6 {

	public static void main(String[] args){

		/*
		try
		{
			PrintStream outstream = null;
			outstream = new PrintStream(new FileOutputStream("input.txt"));
			System.setOut(outstream);
		}
		catch (FileNotFoundException ex)
		{
			// insert code to run when exception occurs
		}*/


		MySolver player =new MySolver();

		// TODO Auto-generated method stub
		Scanner input = new Scanner(System.in);
		System.out.println("我方是什麼顏色 (1黑色 (2白色");
		int firstPerson=input.nextInt();
		while(true) {
			if(firstPerson==1) {
				player.SetMarkTake(1);
				player.Mark(new Move(10,10));

				//兩個電腦pk時請加上這行
				//player.play();

				player.PrintBoard(player.GetCurrentState());
				System.out.println("對方第一顆只:");
                player.InputPosition();
                System.out.println("對方第二顆只:");
                player.InputPosition();
				break;
			}else if(firstPerson==2) {
				player.SetMarkTake(2);
				player.InputPosition();
				break;
			}else {

				System.out.println("輸入錯誤請重新輸入");
			}
		}
		player.PrintBoard(player.GetCurrentState());
		while(true)
		{
			String message =player.play();
			player.PrintBoard(player.GetCurrentState());
			if(message=="lost")
			{
                System.out.println("我方輸");
				break;
			}
			if(message=="win") {
                System.out.println("你輸了~你好廢喔");
				break;
			}
			else
				System.out.println(message);

			System.out.println("對方第一顆只:");
			player.InputPosition();
			System.out.println("對方第二顆只:");
			player.InputPosition();
			player.PrintBoard(player.GetCurrentState());


		}
		firstPerson=input.nextInt();
	}

}
