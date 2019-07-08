package cn.cnnic.domainstat;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.cnnic.domainstat.utils.StringUtil;

public class Test {

	public static void main(String[] args) throws IOException, InterruptedException {
		Runtime rt = Runtime.getRuntime();
		Process proc = rt.exec("/home/zhangt/dest/sql.bin user='' log='/home/zhang/dest/sql.log'");
		int exitVal = proc.waitFor();
		System.out.println("Process exitValue: " + exitVal);
	}

}
