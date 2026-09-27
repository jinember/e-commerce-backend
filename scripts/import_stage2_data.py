# -*- coding: utf-8 -*-
"""
第二阶段数据导入清洗脚本
处理的坑：
  1. 样本文件 GBK 编码（不是 UTF-8）
  2. 行为日志是 Tab 分隔，评价是逗号 CSV
  3. 样本 ID 体系(U1000x/P1000x/O20260911xxx) -> 数据库真实 ID(member_id/spu_id/order_id)
  4. 中文行为类型 -> 英文枚举(view/search/favorite/cart/buy)
  5. behavior_time/review_time -> create_date(datetime)
输出：import_stage2_data.sql (utf8mb4)，再用 mysql 客户端执行。
"""
import csv, os, datetime

BASE = r"D:\e-commerce\resource\20260914"
BEHAVIOR_FILE = os.path.join(BASE, "01-用户行为日志样本.txt")
REVIEW_FILE   = os.path.join(BASE, "03_用户评价数据样本.csv")
OUT_SQL       = r"D:\code\Java\Idea project\e-commerce\scripts\import_stage2_data.sql"

# ---------- 映射规则（透明、可改） ----------
# 用户: U10001 -> member.id 1, U10002 -> 2 ... (member 表前5条: 张三/李四/王五/赵六/孙七)
def map_user(uid):
    # uid 形如 U10001 -> 取末尾数字
    try:
        return int(uid[1:]) - 10000   # U10001 -> 1
    except Exception:
        return None

# 商品: P10001..P10010 -> 现有 spu.id(升序 [1,5,6,7,8,9,10,11,12,13])
SPU_IDS = [1, 5, 6, 7, 8, 9, 10, 11, 12, 13]
def map_spu(pid):
    try:
        idx = int(pid[1:]) - 10001    # P10001 -> 0
        return SPU_IDS[idx] if 0 <= idx < len(SPU_IDS) else None
    except Exception:
        return None

# 订单: O202609110001 -> tbl_order_info.id 1 (取末尾序号)
def map_order(oid):
    try:
        return int(oid[-4:])          # O202609110001 -> 1
    except Exception:
        return None

# 中文行为类型 -> 英文枚举
BEHAVIOR_MAP = {
    "浏览": "view", "浏览商品": "view",
    "搜索": "search",
    "收藏": "favorite",
    "加入购物车": "cart", "加购物车": "cart", "加入购物": "cart",
    "购买": "buy", "下单": "buy",
}

def esc(s):
    if s is None:
        return "NULL"
    return "'" + str(s).replace("\\", "\\\\").replace("'", "\\'") + "'"

def parse_time(s):
    s = (s or "").strip()
    for fmt in ("%Y/%m/%d %H:%M", "%Y-%m-%d %H:%M:%S", "%Y/%m/%d %H:%M:%S"):
        try:
            return datetime.datetime.strptime(s, fmt).strftime("%Y-%m-%d %H:%M:%S")
        except ValueError:
            continue
    return None

def page_url_for(btype, spu_id, keyword):
    if btype == "view":
        return f"/detail/{spu_id}"
    if btype == "search":
        return f"/search?keyword={keyword or ''}"
    return None

sql_lines = []
sql_lines.append("USE `mall-sys`;")
sql_lines.append("SET NAMES utf8mb4;")

# ---------- 1. 用户行为日志 (Tab 分隔, GBK) ----------
beh_rows = []
with open(BEHAVIOR_FILE, "r", encoding="gbk", newline="") as f:
    reader = csv.DictReader(f, delimiter="\t")
    for r in reader:
        member_id = map_user(r["user_id"])
        spu_id    = map_spu(r["product_id"])
        btype     = BEHAVIOR_MAP.get(r["behavior_type"].strip(), r["behavior_type"].strip())
        cdate     = parse_time(r["behavior_time"])
        device    = r["device_type"].strip() or None
        session   = (r["session_id"] or "").strip() or None
        keyword   = (r["search_keyword"] or "").strip() or None
        purl      = page_url_for(btype, spu_id, keyword)
        beh_rows.append((member_id, session, spu_id, btype, keyword, purl, device, cdate))

sql_lines.append("")
sql_lines.append("-- ===== 用户行为 tbl_user_behavior (%d 行) =====" % len(beh_rows))
sql_lines.append("INSERT INTO tbl_user_behavior")
sql_lines.append("  (member_id, session_id, spu_id, behavior_type, search_keyword, page_url, device, create_date, update_date)")
sql_lines.append("VALUES")
vals = []
for (mid, sess, spu, bt, kw, purl, dev, cd) in beh_rows:
    vals.append("  (%s, %s, %s, %s, %s, %s, %s, %s, %s)" % (
        esc(mid), esc(sess), esc(spu), esc(bt), esc(kw), esc(purl), esc(dev),
        esc(cd), esc(cd)))
sql_lines.append(",\n".join(vals) + ";")

# ---------- 2. 用户评价 (逗号 CSV, GBK) ----------
rev_rows = []
with open(REVIEW_FILE, "r", encoding="gbk", newline="") as f:
    reader = csv.DictReader(f)
    for r in reader:
        order_id  = map_order(r["order_id"])
        member_id = map_user(r["user_id"])
        spu_id    = map_spu(r["product_id"])
        rating    = int(r["rating"]) if r["rating"].strip().isdigit() else 5
        content   = (r["review_content"] or "").strip()
        cdate     = parse_time(r["review_time"])
        rev_rows.append((order_id, spu_id, member_id, content, rating, cdate))

sql_lines.append("")
sql_lines.append("-- ===== 用户评价 tbl_comment (%d 行) =====" % len(rev_rows))
sql_lines.append("INSERT INTO tbl_comment")
sql_lines.append("  (order_id, spu_id, member_id, content, rating, create_date, update_date)")
sql_lines.append("VALUES")
vals = []
for (oid, spu, mid, content, rating, cd) in rev_rows:
    vals.append("  (%s, %s, %s, %s, %s, %s, %s)" % (
        esc(oid), esc(spu), esc(mid), esc(content), esc(rating), esc(cd), esc(cd)))
sql_lines.append(",\n".join(vals) + ";")

# ---------- 写出 ----------
os.makedirs(os.path.dirname(OUT_SQL), exist_ok=True)
with open(OUT_SQL, "w", encoding="utf-8") as f:
    f.write("\n".join(sql_lines) + "\n")

# ---------- 控制台核对（按 gbk 输出避免乱码） ----------
print("=== 行为日志解析结果 ===")
for row in beh_rows:
    print(row)
print("=== 评价解析结果 ===")
for row in rev_rows:
    print(row)
print("\nSQL 已生成:", OUT_SQL)
