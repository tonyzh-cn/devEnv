package cn.cnnic.domainstat.service;

import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cn.cnnic.domainstat.mapper.CDomainMapper;
import cn.cnnic.domainstat.mapper.EppContactMapper;
import cn.cnnic.domainstat.mapper.EppEntDomainMapper;
import cn.cnnic.domainstat.po.CDomainPO;
import cn.cnnic.domainstat.po.EppContactPO;
import cn.cnnic.domainstat.po.EppEntAllDomainPO;
import cn.cnnic.domainstat.utils.CalendarUtil;
import cn.cnnic.domainstat.utils.EmailUtil;
import cn.cnnic.domainstat.utils.FileUtil;
import cn.cnnic.domainstat.utils.SpMap;
import cn.cnnic.domainstat.utils.SpReMap;
import net.cnnic.borlan.utils.lookup.ChinesePostalCodeUtil;
import net.cnnic.borlan.utils.lookup.PostalCodeUtil;

@Service
public class DomainStatService {
	@Autowired
	private CDomainMapper cDomainMapper;
	@Autowired
	private EppContactMapper contactMapper;
	@Autowired
	private EppEntDomainMapper entDomainMapper;
	@Autowired
	private SpMap spMap;
	@Autowired
	private SpReMap spReMap;

	private PostalCodeUtil pcu;
	private static final String RESULT_ROOT_PATH=DomainStatService.class.getResource("/").getFile()+"/result/";

	public DomainStatService() {
		pcu = new ChinesePostalCodeUtil();
	}

	public void fetchData() throws Exception {
		fetchCdnCn();
		fetchCdnZhongguo();
		fetchCn();
	}

	private void fetchCn() throws Exception {
		fetchCn(null,"2009-12-31","before2010.txt");
		for(int year=2010;year<=CalendarUtil.getCurrentYear();year++) {
			fetchCn(year+"-01-01",year+"-12-31",year+".txt");
		}
	}

	private void fetchCn(String startDate, String endDate,String resultFileName) throws Exception {
		int count=entDomainMapper.queryCount( startDate, endDate);
		List<EppEntAllDomainPO> entDomainList = entDomainMapper.query(startDate, endDate);
		boolean isValid=false;
		for(int retry=0;retry<3;retry++) {
			if(Math.abs(count-entDomainList.size())>1000) {
				entDomainList = entDomainMapper.query(startDate, endDate);
				isValid=false;
			}else {
				isValid=true;
				break;
			}
		}
		if(!isValid) {
			EmailUtil.sendEmail(new String[] {"zhangtao@cnnic.cn"}, startDate+"至"+endDate+"数据调取异常！", "");
		}
		FileUtil.init(RESULT_ROOT_PATH+resultFileName);
		for (EppEntAllDomainPO domainPO : entDomainList) {
			String domainName = domainPO.getDomainName();
			String registrarId = domainPO.getSponsorRegrid();
			String registrantId = domainPO.getRegistrantId();
			String province=buildProvince(registrantId);
			FileUtil.writeFile(domainName + "\t" + registrarId + "\t" + province + "\t");
		}
		FileUtil.commit();	
	}

	private void fetchCdnZhongguo() throws Exception {
		writeCdn("cdn-zhongguo.txt","%.中国");
	}

	private void fetchCdnCn() throws Exception {
		writeCdn("cdn-cn.txt","%.cn");
	}
	
	private void writeCdn(String resultFileName, String likePattern) throws Exception {
		int count=cDomainMapper.queryCount(null, CalendarUtil.format(new Date(), "yyyy-MM-dd"), likePattern);
		List<CDomainPO> cDomainList = cDomainMapper.query(null, CalendarUtil.format(new Date(), "yyyy-MM-dd"), likePattern);
		
		boolean isValid=false;
		for(int retry=0;retry<3;retry++) {
			if(Math.abs(count-cDomainList.size())>1000) {
				cDomainList = cDomainMapper.query(null, CalendarUtil.format(new Date(), "yyyy-MM-dd"), likePattern);
				isValid=false;
			}else {
				isValid=true;
				break;
			}
		}
		if(!isValid) {
			EmailUtil.sendEmail(new String[] {"zhangtao@cnnic.cn"}, resultFileName.split(".")[0]+"数据调取异常！", "");
		}
		FileUtil.init(RESULT_ROOT_PATH+resultFileName);
		for (CDomainPO domainPO : cDomainList) {
			String domainName = domainPO.getDomainName();
			String registrarId = domainPO.getRegistrarId();
			String registrantId = domainPO.getRegistrantId();
			String province=buildProvince(registrantId);
			FileUtil.writeFile(domainName + "\t" + registrarId + "\t" + province + "\t");
		}
		FileUtil.commit();		
	}

	private String buildProvince(String registrantId) {
		EppContactPO contactPO = contactMapper.query(registrantId);
		String registrantPC = contactPO.getAddrPc();
		String registrantSP = contactPO.getAddrSp();
		String registrantStreet = contactPO.getAddrStreet();
		if (registrantPC == null || registrantPC.trim().equals("")) {
			registrantPC = "999999";
		}
		if (registrantSP == null || registrantSP.trim().equals("")) {
			registrantSP = "NO_STATEPROVINCE";
		}
		String province = pcu.getProvince(registrantPC).toLowerCase();
		if (province.equals("unknown")) {
			for (String key : spMap.keySet()) {
				Pattern pattern = Pattern.compile("^(中国)?[\\s\\.·?，．]?"+key+"省?",
						Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				Matcher matcher = pattern.matcher(registrantStreet);
				if (matcher.matches()) {
					province = spMap.get(key);
					break;
				}
			}
		}
		// according to address
		if (province.equals("unknown")) {
			for (String key : spMap.keySet()) {
				Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				Matcher matcher = pattern.matcher(registrantStreet);
				if (matcher.matches()) {
					province = spMap.get(key);
					break;
				}
			}
		}

		// according to province fields
		if (province.equals("unknown") && !registrantSP.equals("NO_STATEPROVINCE")) {
			for (String key : spReMap.keySet()) {
				Pattern pattern = Pattern.compile(key, Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
				Matcher matcher = pattern.matcher(registrantSP);
				if (matcher.matches()) {
					province = spReMap.get(key);
					break;
				}
			}
		}
		if ("tw".equals(province) || "mo".equals(province) || "hk".equals(province) || "foreign".equals(province)) {
			province = "unknown";
		}
		return province;
	}

}
