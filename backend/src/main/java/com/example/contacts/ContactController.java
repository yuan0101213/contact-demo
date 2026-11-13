package com.example.contacts;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/contacts")
public class ContactController {
    private final ContactRepository repository;

    public ContactController(ContactRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<Contact> list(@RequestParam(defaultValue = "") String keyword) {
        return repository.findAll(keyword.strip());
    }

    @GetMapping("/{id}")
    public Contact get(@PathVariable long id) {
        return repository.findById(id).orElseThrow(this::missing);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Contact create(@Valid @RequestBody ContactInput input) { return repository.create(input); }

    @PutMapping("/{id}")
    public Contact update(@PathVariable long id, @Valid @RequestBody ContactInput input) {
        return repository.update(id, input).orElseThrow(this::missing);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        if (!repository.delete(id)) throw missing();
    }

    private ResponseStatusException missing() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "联系人不存在，请刷新列表");
    }

    public record Contact(long id, String name, String email, String phone, String note) {}

    public record ContactInput(
            @NotBlank(message = "请输入姓名") @Size(max = 30, message = "姓名最多 30 字") String name,
            @NotBlank(message = "请输入邮箱") @Email(message = "邮箱格式不正确") @Size(max = 100) String email,
            @NotBlank(message = "请输入手机号") @Pattern(regexp = "1[3-9]\\d{9}", message = "请输入 11 位中国大陆手机号") String phone,
            @Size(max = 100, message = "备注最多 100 字") String note) {}
}
