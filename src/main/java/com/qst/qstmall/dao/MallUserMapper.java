package com.qst.qstmall.dao;

import com.qst.qstmall.entity.MallUser;
import com.qst.qstmall.utils.PageQueryUtil;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MallUserMapper {

    /*注册，插入一个用户*/
    int insertSelective(MallUser record);

    /*根据用户名返回一个用户*/
    MallUser selectByLoginName(String loginName);


    MallUser selectByLoginNameAndPasswd(@Param("loginName") String loginName, @Param("password") String password);

    int updateByPrimaryKeySelective(MallUser record);

    MallUser selectByPrimaryKey(Long userId);

    List<MallUser> findMallUserList(PageQueryUtil pageUtil);

    int getTotalMallUsers(PageQueryUtil pageUtil);

    int lockUserBatch(@Param("ids") Integer[] ids, @Param("lockStatus") int lockStatus);
}