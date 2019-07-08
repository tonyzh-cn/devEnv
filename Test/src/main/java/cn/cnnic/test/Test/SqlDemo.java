package cn.cnnic.test.Test;

import java.io.IOException;

public class SqlDemo {

	public static void main(String[] args) throws IOException {
		String userDir=System.getProperty("user.dir");
		FileUtil.init(userDir+"/domain.sql");
		for(int i=1;i<=1000000;i++) {
			FileUtil.writeFile("insert into domain(name,contact_id) values('domain"+i+"',"+i+");\n");
		}
		FileUtil.commit();
	}

}
