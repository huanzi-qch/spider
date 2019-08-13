package cn.huanzi.qch.commonspider.repository;

import cn.huanzi.qch.commonspider.pojo.IpProxy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface IpProxyPoolRepository extends JpaRepository<IpProxy,String>, JpaSpecificationExecutor<IpProxy> {
}
