package com.ydles.user.controller;
import com.ydles.entity.PageResult;
import com.ydles.entity.Result;
import com.ydles.entity.StatusCode;
import com.ydles.order.pojo.Task;
import com.ydles.user.config.TokenDecode;
import com.ydles.user.service.UserService;
import com.ydles.user.pojo.User;
import com.github.pagehelper.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
@RestController
@CrossOrigin
@RequestMapping("/user")
public class UserController {


    @Autowired
    private UserService userService;

    /**
     * 查询全部数据
     * @return
     */
    @PreAuthorize("hasAnyAuthority('seckill_list')")
    @GetMapping
    public Result findAll(){
        List<User> userList = userService.findAll();
        return new Result(true, StatusCode.OK,"查询成功",userList) ;
    }

    /***
     * 根据ID查询数据
     * @param username
     * @return
     */
    @PreAuthorize("hasAnyAuthority('user_list')")
    @GetMapping("/{username}")
    public Result findById(@PathVariable String username){
        User user = userService.findById(username);
        return new Result(true,StatusCode.OK,"查询成功",user);
    }


    /***
     * 新增数据
     * @param user
     * @return
     */
    @PostMapping
    public Result add(@RequestBody User user){
        userService.add(user);
        return new Result(true,StatusCode.OK,"添加成功");
    }


    /***
     * 修改数据
     * @param user
     * @param username
     * @return
     */
    @PutMapping(value="/{username}")
    public Result update(@RequestBody User user,@PathVariable String username){
        user.setUsername(username);
        userService.update(user);
        return new Result(true,StatusCode.OK,"修改成功");
    }


    /***
     * 根据ID删除品牌数据
     * @param username
     * @return
     */
    @DeleteMapping(value = "/{username}" )
    public Result delete(@PathVariable String username){
        userService.delete(username);
        return new Result(true,StatusCode.OK,"删除成功");
    }

    /***
     * 多条件搜索品牌数据
     * @param searchMap
     * @return
     */
    @GetMapping(value = "/search" )
    public Result findList(@RequestParam Map searchMap){
        List<User> list = userService.findList(searchMap);
        return new Result(true,StatusCode.OK,"查询成功",list);
    }


    /***
     * 分页搜索实现
     * @param searchMap
     * @param page
     * @param size
     * @return
     */
    @GetMapping(value = "/search/{page}/{size}" )
    public Result findPage(@RequestParam Map searchMap, @PathVariable  int page, @PathVariable  int size){
        Page<User> pageList = userService.findPage(searchMap, page, size);
        PageResult pageResult=new PageResult(pageList.getTotal(),pageList.getResult());
        return new Result(true,StatusCode.OK,"查询成功",pageResult);
    }
    @GetMapping("/load/{username}")
    public User findUserInfo(@PathVariable("username") String username){
        User user = userService.findById(username);
        return user;
    }

    @Autowired
    TokenDecode tokenDecode;
    //增加用户积分
    @GetMapping("/points/add")
    public Result addPoints(Integer points){
        //获取用户名
        Map<String, String> userMap = tokenDecode.getUserInfo();
        String username = userMap.get("username");

        //添加积分
        userService.addUserPoints(username,points);
        return new Result(true,StatusCode.OK,"添加积分成功！");
    }
}
