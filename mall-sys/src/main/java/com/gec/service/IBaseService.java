package com.gec.service;

/*
 * 主要用途: (通用节点)
 * 1. 处理树形节点的数据(解析, 封装)。
 * 2. 组织相关数据, 生成树形结构。
 * 3. 甚至可以实现查找功能。
 * 4. 对一个树形结构进行 增, 删, 改.
 *
 * [+]一级部门: 总经办
 *    [+]二级部门: 行政部
 *                人事部
 *    [+]三级部门
 *
 * [+]一级部门: 公司工会(委员会)
 *    [+]二级部门: 行政部
 * [+]一级部门: 董事会
 *
 */

import com.gec.domain.entity.Node;

import java.util.ArrayList;
import java.util.List;

public interface IBaseService {
    /*
    * 1.原本的 list 没有分层结构处理的。
    * 2.这里给它做一下分层处理, 转换为带分层结构的列表。
    * 一句话: 把线性结构 ==> 层次结构。
    */

    /* 1.请实现方法1. */
    default List<Node> convertNodeBO(
            List<? extends Node> list
    ){
//        1.定义一个顶层节点ID：0。
        Integer topId = 0;
//        2.定义第一层列表。
        List oneList = new ArrayList<>();
//        3.迭代原始列表。
        for (Node node: list) {
//            4.获取每一个节点父id。
            Integer parID = node.getParentId();
//            5.当前节点父id == topID,表示他是一级节点。
            if(parID != null && parID.equals(topId)){
                Node nodeBO = copyObj(node);
//                6.查找子节点。
                findChildren(nodeBO,list);
//                7.添加到一级列表。
                oneList.add(nodeBO);
            }
        }
        return oneList;
    }

	/* 2.请实现方法2. */
    default void findChildren(
            Node D ,List<? extends Node> list){
        /*1.获取D的ID，作为parentID。*/
        Integer parentId = D.getId();
        /*2.迭代原始列表*/
        for (Node node : list) {
            /*3.获取每一个节点父id。*/
            Integer parID = node.getParentId();
            /*4.当前节点父id == D.ID，表示他是D的下一级节点。*/
            if(parID != null && parID.equals(parentId)){
                Node nodeBO = copyObj(node);
                /*5.查找子节点。*/
                findChildren(nodeBO,list);
                /*6.添加D的子列表。*/
                D.addChildNode(nodeBO);
            }
        }
    }


    //这个方法是在实现类中实施。
    Node copyObj(Node node);
}
