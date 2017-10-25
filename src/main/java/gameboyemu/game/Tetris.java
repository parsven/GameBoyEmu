package gameboyemu.game;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Tetris {
	private static byte[] romContent__ = null;

	static {
		try {
			romContent__ = read("TETRIS.GB");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] getRomContent() {
		byte[] romContent = new byte[romContent__.length];
		System.arraycopy(romContent__,0,romContent, 0, romContent__.length);
		return romContent;
	}
	private static byte[] read(String fileName) throws IOException {

		Path currentRelativePath = Paths.get("");
		String s = currentRelativePath.toAbsolutePath().toString();
		System.out.println("Current relative path is: " + s);

		File file = new File(fileName);
		FileInputStream fin = new FileInputStream(file);
		int len = (int) file.length();
		byte fileContent[] = new byte[len];
		int readSoFar = 0;
		do {
			readSoFar += fin.read(fileContent, readSoFar, len - readSoFar);

		} while(len - readSoFar > 0);
		return fileContent;
	}
}
