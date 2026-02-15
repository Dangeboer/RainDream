package com.dangeboer.raindream.model.form;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ItemBatchForm extends ItemForm {
    List<String> storeUrls;
    List<Long> sizeBytesList;
}
