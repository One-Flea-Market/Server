package com.server.response;

import com.server.model.BoardDTO;
import com.server.model.ProductDTO;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MessageResProduct {
    private boolean result;
    private String message;
    private boolean onself;
    private boolean onlike;
    private List<ProductDTO> productList;
    public MessageResProduct() {
        this.result = false;
        this.message = null;
        this.onself = false;
        this.onlike = false;
        this.productList = new ArrayList<>();
    }
}
