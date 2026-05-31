package com.campus_trade.service;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class AiAuditService {

    private static final Logger log = LoggerFactory.getLogger(AiAuditService.class);

    @Value("${ai-audit.enabled:true}")
    private boolean enabled;

    @Value("${ai-audit.auto-pass:true}")
    private boolean autoPass;

    @Value("${ai-audit.auto-reject:false}")
    private boolean autoReject;

    private final Set<String> sensitiveWords = new HashSet<>();
    private final Map<String, String> sensitivePatterns = new LinkedHashMap<>();

    @PostConstruct
    public void init() {
        // Categories of prohibited items / content
        addPatterns("违禁品类", "枪支|弹药|管制刀具|毒品|大麻|海洛因|冰毒|摇头丸|迷药|麻醉");
        addPatterns("违法服务", "代考|替考|代写论文|办假证|黑客|刷单|洗钱|套现|发票代开");
        addPatterns("色情低俗", "色情|裸聊|约炮|一夜情|成人用品违禁|原味|偷拍|走光");
        addPatterns("假冒伪劣", "高仿|A货|精仿|原单|尾单|复刻|1:1|超A");
        addPatterns("赌博相关", "赌博|赌球|六合彩|时时彩|百家乐|老虎机|棋牌代理");
        addPatterns("医疗相关", "处方药|伟哥|催情|迷幻|三无药品|非法医疗|干细胞");
        addPatterns("野生动物", "象牙|犀牛角|虎骨|穿山甲|玳瑁|珊瑚|珍稀动物");
        addPatterns("个人信息", "身份证|银行卡|手机卡|实名认证代过人脸|银行四件套");
        addPatterns("校园违禁", "作弊|逃课|代课|代签到|代点名|校园贷|裸贷");
        addPatterns("其他违规", "传销|直销|拉人头|资金盘|虚拟币|炒币|挖矿");

        log.info("AI audit system initialized with {} pattern categories", sensitivePatterns.size());
    }

    private void addPatterns(String category, String pattern) {
        sensitivePatterns.put(category, pattern);
        for (String word : pattern.split("\\|")) {
            sensitiveWords.add(word.toLowerCase());
        }
    }

    /**
     * Audit product content. Returns audit result.
     */
    public AuditResult audit(String title, String description) {
        if (!enabled) {
            return AuditResult.pass();
        }

        String content = (title + " " + (description != null ? description : "")).toLowerCase();
        List<String> violations = new ArrayList<>();

        for (Map.Entry<String, String> entry : sensitivePatterns.entrySet()) {
            String category = entry.getKey();
            for (String word : entry.getValue().split("\\|")) {
                if (content.contains(word.toLowerCase())) {
                    violations.add(category + ": " + word);
                    break; // one match per category is enough
                }
            }
        }

        if (violations.isEmpty()) {
            log.info("AI audit PASSED for product");
            return AuditResult.pass();
        }

        String reason = "检测到以下违规内容: " + String.join(", ", violations);
        log.warn("AI audit REJECTED: {}", reason);

        if (autoReject) {
            return AuditResult.reject(reason);
        }

        // Default: flag for human review
        return AuditResult.review(reason);
    }

    public record AuditResult(String result, String reason) {
        public static AuditResult pass() {
            return new AuditResult("PASS", null);
        }
        public static AuditResult review(String reason) {
            return new AuditResult("REVIEW", reason);
        }
        public static AuditResult reject(String reason) {
            return new AuditResult("REJECT", reason);
        }
    }
}
