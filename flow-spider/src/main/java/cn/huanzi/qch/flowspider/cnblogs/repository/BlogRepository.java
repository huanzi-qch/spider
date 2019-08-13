package cn.huanzi.qch.flowspider.cnblogs.repository;

import cn.huanzi.qch.flowspider.cnblogs.pojo.Blog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogRepository extends JpaRepository<Blog,String>, JpaSpecificationExecutor<Blog> {
}
