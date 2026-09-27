package com.gec.controller;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gec.components.FileTemplate;
import com.gec.domain.entity.Invoice;
import com.gec.service.IInvoiceService;
import com.gec.util.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/Invoice")
public class InvoiceController extends BaseController {

    @Autowired
    private IInvoiceService Service;
    @Autowired
    private FileTemplate fileTemplate;

    //发票文件（PDF）上传子目录
    private String SUB_DIR = "invoice";

    @Override
    protected FileTemplate getFileTemplate() { return fileTemplate; }

    @PostMapping("/list/{page}/{limit}")
    public R list(@PathVariable Integer page, @PathVariable Integer limit, @RequestBody Invoice param) {
        Page<Invoice> p = new Page<>(page, limit);
        return R.convertPage(Service.listInvoice(p));
    }

    @PostMapping("/save")
    public R save(@RequestBody Invoice obj) { Service.saveOrUpdate(obj); return R.ok(); }

    @PostMapping("/issue")
    public R issue(@RequestBody Invoice obj) {
        try {
            Service.issue(obj);
            return R.ok();
        } catch (Exception e) {
            return R.err(e);
        }
    }

    @PostMapping("/delete/{id}")
    public R delete(@PathVariable Integer id) { Service.removeById(id); return R.ok(); }

    @GetMapping("/{id}")
    public R getById(@PathVariable Integer id) { return R.ok(Service.getById(id)); }

    /**
     * 发票 PDF 上传。返回文件名（落库）与访问 URI（前端展示）。
     */
    @PostMapping("/upload")
    public R upload(@RequestParam("file") MultipartFile mFile) {
        if (mFile == null || mFile.isEmpty()) {
            throw new RuntimeException("文件不能为空");
        }
        String original = mFile.getOriginalFilename();
        String ext = FileUtils.extName(original);
        if (!".pdf".equalsIgnoreCase(ext)) {
            throw new RuntimeException("只允许上传 PDF 格式的发票文件");
        }
        String newName = FileUtils.makeUUID() + ".pdf";
        try {
            fileTemplate.setMultipartFile(mFile);
            fileTemplate.saveFile(SUB_DIR, newName);
            return R.ok()
                    .put("fileName", newName)
                    .put("fileUri", "/Invoice/showFile/" + newName);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("发票文件上传失败");
        }
    }

    /**
     * 读取/预览发票 PDF。浏览器内置阅读器直接打开。
     * 带 download=1 时以附件形式下载。
     */
    @GetMapping("/showFile/{fileName}")
    public void showFile(@PathVariable String fileName,
                         @RequestParam(value = "download", required = false) Integer download,
                         HttpServletResponse resp) {
        try {
            byte[] data = fileTemplate.getFile(SUB_DIR, fileName);
            resp.setContentType("application/pdf");
            if (download != null && download == 1) {
                resp.setHeader("Content-Disposition", "attachment; filename=\"invoice-" + fileName + "\"");
            } else {
                resp.setHeader("Content-Disposition", "inline; filename=\"" + fileName + "\"");
            }
            resp.getOutputStream().write(data);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                resp.setStatus(404);
                resp.setContentType("text/plain;charset=UTF-8");
                resp.getWriter().write("发票文件不存在");
            } catch (Exception ignored) {}
        }
    }
}
