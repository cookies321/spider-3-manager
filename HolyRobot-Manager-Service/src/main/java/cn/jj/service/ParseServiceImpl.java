package cn.jj.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.holyrobot.data.Param;
import com.holyrobot.data.Params;

@Service
public class ParseServiceImpl implements IParseService{
	
	@Autowired
	private ParseLvmama parseLvmama;
	
	@Autowired
	private ParseCtrip parseCtrip;
	
	@Autowired
	private ParseTuniu parseTuniu;
	
	@Autowired
	private ParseTongcheng parseTongcheng;
	
	@Autowired
	private ParseQunaer parseQunaer;

	@Override
	public void parse(Params params) {
		Param dataSource = params.getDataSource();
		switch (dataSource) {
		case LVMAMA:
			parseLvmama(params);
			break;
		case CTRIP:
			parseCtrip(params);
			break;
		case TUNIU:
			parseTuniu(params);
			break;
		case TONGCHENG:
			parseTongCheng(params);
			break;
		case QUNAER:
			parseQunaer(params);
			break;
		default:
			break;
		}
			
		
	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月8日 下午2:12:58
	 * @action parseQunaer
	 * @param params
	 */
	private void parseQunaer(Params params) {
		Param type = params.getType();
		switch (type) {
		case QUNAER_STROKE_FIRST:
			parseQunaer.parseQunaerStrokeFirst(params);
			break;
		case QUNAER_STROKE_PAGE:
			parseQunaer.parseQunaerStrokePage(params);
			break;
		case QUNAER_STROKE_DETAIL:
			parseQunaer.parseQunaerStrokeDetail(params);
			break;
		case QUNAER_STROKE_COMMENT:
			parseQunaer.parseQunaerStrokeComment(params);
			break;
		default:
			break;
		}
	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月8日 下午2:12:42
	 * @action parseTongCheng
	 * @param params
	 */
	private void parseTongCheng(Params params) {
		Param type = params.getType();
		switch (type) {
		case TONGCHENG_STROKE_FIRST:
			parseTongcheng.parseTongchengStrokeFirst(params);
			break;
		case TONGCHENG_STROKE_PAGE:
			parseTongcheng.parseTongchengStrokePage(params);
			break;
		case TONGCHENG_STROKE_DETAIL:
			parseTongcheng.parseTongchengStrokeDetail(params);
			break;
		case TONGCHENG_STROKE_COMMENT_FIRST:
			parseTongcheng.parseTongchengStrokeCommentFirst(params);
			break;
		case TONGCHENG_STROKE_COMMENT:
			parseTongcheng.parseTongchengStrokeComment(params);
			break;
		case TONGCHENG_SCENIC_FIRST:
			parseTongcheng.parseTongchengScenicFirst(params);
			break;
		case TONGCHENG_SCENIC_PAGE:
			parseTongcheng.parseTongchengScenicPage(params);
			break;
		case TONGCHENG_SCENIC_DETAIL:
			parseTongcheng.parseTongchengScenicDetail(params);
			break;
		case TONGCHENG_SCENIC_COMMENT_FIRST:
			parseTongcheng.parseTongchengScenicCommentFirst(params);
			break;
		case TONGCHENG_SCENIC_BOOKNOWNEW:
			parseTongcheng.parseTongchengScenicBoowNowNew(params);
			break;
		case TONGCHENG_SCENIC_COMMENT:
			parseTongcheng.parseTongchengScenicComment(params);
			break;
		case TONGCHENG_SCENIC_PRICE:
			parseTongcheng.parseTongchengScenicPrice(params);
			break;
		default:
			break;
		}
	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月8日 下午2:12:31
	 * @action parseTuniu
	 * @param params
	 */
	private void parseTuniu(Params params) {
		Param type = params.getType();
		switch (type) {
		case TUNIU_STROKE_FIRST:
			parseTuniu.parseTuniuStrokeFirst(params);
			break;
		case TUNIU_STROKE_DETAIL:
			parseTuniu.parseTuniuStrokeDetail(params);
			break;
		case TUNIU_STROKE_COMMENT:
			parseTuniu.parseTuniuStrokeComment(params);
			break;
		case TUNIU_SCENIC_FIRST:
			parseTuniu.parseTuniuScenicFirst(params);
			break;
		case TUNIU_SCENIC_DETAIL:
			parseTuniu.parseTuniuScenicDetail(params);
			break;
		case TUNIU_SCENIC_COMMENT:
			parseTuniu.parseTuniuScenicComment(params);
			break;
		case TUNIU_ROUTE_FIRST:
			parseTuniu.parseTuniuRouteFirst(params);
			break;
		case TUNIU_ROUTE_PAGE:
			parseTuniu.parseTuniuRoutePage(params);
			break;
		case TUNIU_ROUTE_DETAIL:
			parseTuniu.parseTuniuRouteDetail(params);
			break;
		case TUNIU_ROUTE_PRICE:
			parseTuniu.parseTuniuRoutePrice(params);
			break;
		case TUNIU_ROUTE_COMMENT:
			parseTuniu.parseTuniuRouteComment(params);
			break;
		case TUNIU_HOTEL_FIRST:
			parseTuniu.parseTuniuHotelFirst(params);
			break;
		case TUNIU_HOTEL_PAGE:
			parseTuniu.parseTuniuHotelPage(params);
			break;
		case TUNIU_HOTEL_DETAIL:
			parseTuniu.parseTuniuHotelDetail(params);
			break;
		case TUNIU_HOTEL_PICTURE:
			parseTuniu.parseTuniuHotelPicture(params);
			break;
		case TUNIU_HOTEL_INTRODUCTION:
			parseTuniu.parseTuniuHotelIntroduction(params);
			break;
		case TUNIU_HOTEL_ROOM:
			parseTuniu.parseTuniuHotelRoom(params);
			break;
		case TUNIU_HOTEL_COMMENT:
			parseTuniu.parseTuniuHotelComment(params);
			break;
		default:
			break;
		}
	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月8日 下午2:12:20
	 * @action parseCtrip
	 * @param params
	 */
	private void parseCtrip(Params params) {
		Param type = params.getType();
		switch (type) {
		case CTRIP_STROKE_FIRST:
			//解析携程景点爬取种子url的首个链接获取分页链接
			parseCtrip.parseCtripStrokeFirst(params);
			break;
		case CTRIP_STROKE_PAGE:
			//解析携程景点的分页url获取种子链接
			parseCtrip.parseCtripStrokePage(params);
			break;
		case CTRIP_STROKE_DETAIL:
			//解析携程景点的种子url获取景点的详情信息
			parseCtrip.parseCtripStrokeDetail(params);
			break;
		case CTRIP_STROKE_WANTANDBEEN:
			//解析携程景点想去人数和去过人数
			parseCtrip.parseCtripStrokeWantAndBeen(params);
			break;
		case CTRIP_STROKE_COMMENT:
			//解析携程景点评论信息
			parseCtrip.parseCtripStrokeComment(params);
			break;
		case CTRIP_SCENIC_FIRST:
			//解析携程门票的首页链接
			parseCtrip.parseCtripScenicFirst(params);
			break;
		case CTRIP_SCENIC_DETAIL:
			parseCtrip.parseCtripScenicDetail(params);
			break;
		case CTRIP_SCENIC_PICTURE:
			parseCtrip.parseCtripScenicPicture(params);
			break;
		case CTRIP_SCENIC_TICKETSTATUTE:
			parseCtrip.parseCtripScenicTicketStatute(params);
			break;
		case CTRIP_SCENIC_COMMENT:
			parseCtrip.parseCtripScenicComment(params);
			break;
		case CTRIP_ROUTE_FIRST:
			parseCtrip.parseCtripRouteFirst(params);
			break;
		case CTRIP_ROUTE_PAGE:
			parseCtrip.parseCtripRoutePage(params);
			break;
		case CTRIP_ROUTE_DETAIL:
			parseCtrip.parseCtripRouteDetail(params);
			break;
		case CTRIP_ROUTE_PRODUCTOUTLINE:
			parseCtrip.parseCtripRouteProductOutline(params);
			break;
		case CTRIP_ROUTE_PRICE:
			parseCtrip.parseCtripRoutePrice(params);
			break;
		case CTRIP_ROUTE_DEPARTURE:
			parseCtrip.parseCtripRouteDeparture(params);
			break;
		case CTRIP_ROUTE_EXPENSEANDRESERVEINFO:
			parseCtrip.parseCtripExpenseAndReserveinfo(params);
			break;
		case CTRIP_ROUTE_COMMENT_FIRST:
			parseCtrip.parseCtripRouteCommentFirst(params);
			break;
		case CTRIP_ROUTE_COMMENT:
			parseCtrip.parseCtripRouteComment(params);
			break;
		case CTRIP_HOTEL_FIRST:
			parseCtrip.parseCtripHotelFirst(params);
			break;
		case CTRIP_HOTEL_PAGE:
			parseCtrip.parseCtripHotelPage(params);
			break;
		case CTRIP_HOTEL_DETAIL:
			parseCtrip.parseCtripHotelDetail(params);
			break;
		case CTRIP_HOTEL_ROOM:
			parseCtrip.parseCtripHotelRoom(params);
			break;
		default:
			break;
		}
	}

	/**
	 * @Description TODO
	 * @author 汤玉林
	 * @date 2018年3月8日 下午2:12:07
	 * @action parseLvmama
	 * @param params
	 */
	private void parseLvmama(Params params) {
		Param type = params.getType();
		switch (type) {
		case LVMAMA_STROKE_FIRST:
			//解析驴妈妈景点爬取种子url的首个链接获取分页链接
			parseLvmama.parseLvmamaStrokeFirst(params);
			break;
		case LVMAMA_STROKE_PAGE:
			//解析驴妈妈景点爬取种子链接url的分页链接
			parseLvmama.parseLvmamaStrokePage(params);
			break;
		case LVMAMA_STROKE_DETAIL:
			//解析驴妈妈景点种子链接获取详情信息
			parseLvmama.parseLvmamaStrokeDetail(params);//方法里面有了下面的两个方法
			//解析驴妈妈景点种子链接获取想去人数和去过人数url
			//parseLvmamaToWantAndBeenURL(params);
			//解析驴妈妈景点种子链接获取评论的url
			//parseLvmamaStrokeToCommentUrl(params);
			break;
		case LVMAMA_STROKE_WANTANDBEEN:
			//解析驴妈妈景点想去人数和去过人数链接获取想去人数和去过人数
			parseLvmama.parseLvmamaStrokeWantAndBeen(params);
			break;
		case LVMAMA_STROKE_COMMENT:
			//解析驴妈妈景点评论的url获取评论的分页url
			parseLvmama.parseLvmamaStrokeToCommentPageUrl(params);
			break;
		case LVMAMA_STROKE_COMMENTPAGE:
			//解析驴妈妈景点评论分页url获取评论信息
			parseLvmama.parseLvmamaStrokeComment(params);
			break;
		case LVMAMA_SCENIC_FIRST:
			//解析驴妈妈门票首页链接获取分页链接
			parseLvmama.parseLvmamaScenicFirst(params);
			break;
		case LVMAMA_SCENIC_DETAIL:
			//解析驴妈妈门票景点详情url获取详细信息
			parseLvmama.parseLvmamaScenicDetail(params);//方法里面有了下面的两个方法

			//解析驴妈妈门票评论url获取评论分页链接
			//parseLvmama.parseLvmamaScenicComment(params);
			break;
		case LVMAMA_SCENIC_FOFAVOUREDPOLICY:
			parseLvmama.parseLvmamaScenicFavouredpolicy(params);
			break;
		case LVMAMA_SCENIC_COMMENTPAGE:
			//解析驴妈妈门票评论分页url
			parseLvmama.parseLvmamaScenicCommentPage(params);
			break;
		case LVMAMA_ROUTE_FIRST:
			//解析驴妈妈行程首页url
			parseLvmama.parseLvmamaRouteFirst(params);
			break;
		case LVMAMA_ROUTE_DETAIL:
			//解析驴妈妈行程详情信息
			parseLvmama.parseLvmamaRouteDetail(params);
			break;
		case LVMAMA_ROUTE_PRICE:
			//解析驴妈妈行程价格url获取行程的价格信息
			parseLvmama.parseLvmamaRoutePrice(params);
			break;
		case LVMAMA_ROUTE_COMMENTPAGE:
			//解析驴妈妈行程评论url，或者行程评论信息
			parseLvmama.parseLvmamaRouteCommentPage(params);
			break;
		case LVMAMA_HOTEL_FIRST:
			//解析驴妈妈酒店首页url
			parseLvmama.parseLvmamaHotelFirst(params);
			break;
		case LVMAMA_HOTEL_PAGE:
			parseLvmama.parseLvmamaHotelPage(params);
			break;
		case LVMAMA_HOTEL_DETAIL:
			//获得驴妈妈酒店房型url
			parseLvmama.parseLvmamaHotelRoomURL(params);
			//解析驴妈妈酒店详情
			parseLvmama.parseLvmamaHotelDetail(params);
			//获得驴妈妈酒店评论url
			parseLvmama.parseLvmamaHotelCommentURL(params);
			break;
		case LVMAMA_HOTEL_ROOM:
			//解析驴妈妈酒店房型信息
			parseLvmama.parseLvmamaHotelRoom(params);
			break;
		case LVMAMA_HOTEL_COMMENT:
			//解析驴妈妈酒店评论信息
			parseLvmama.parseLvmamaHotelComment(params);
			break;
		default:
			break;
		}
	}
}
