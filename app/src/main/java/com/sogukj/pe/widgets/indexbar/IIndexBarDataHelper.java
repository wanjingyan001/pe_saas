package com.sogukj.pe.widgets.indexbar;

import java.util.List;

/**
 * Created by CH-ZH on 2018/8/28.
 */
public interface IIndexBarDataHelper {
    //汉语-》拼音
    IIndexBarDataHelper convert(List<? extends BaseIndexPinyinBean> data);

    //拼音->tag
    IIndexBarDataHelper fillInexTag(List<? extends BaseIndexPinyinBean> data);

    //对源数据进行排序（RecyclerView）
    IIndexBarDataHelper sortSourceDatas(List<? extends BaseIndexPinyinBean> datas);

    //对IndexBar的数据源进行排序(右侧栏),在 sortSourceDatas 方法后调用
    IIndexBarDataHelper getSortedIndexDatas(List<? extends BaseIndexPinyinBean> sourceDatas, List<String> datas);
}
