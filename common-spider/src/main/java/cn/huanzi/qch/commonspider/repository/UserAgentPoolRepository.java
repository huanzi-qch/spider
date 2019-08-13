package cn.huanzi.qch.commonspider.repository;

import cn.huanzi.qch.commonspider.pojo.UserAgent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAgentPoolRepository extends JpaRepository<UserAgent,String>, JpaSpecificationExecutor<UserAgent> {
}
