export const contentTypeOptions = [
  { value: 1, label: "文章" },
  { value: 2, label: "图绘" },
  { value: 3, label: "精修" },
  { value: 4, label: "混剪" },
  { value: 5, label: "解析" },
  { value: 6, label: "吐槽" },
  { value: 7, label: "主创说" },
  { value: 8, label: "RPS" },
  { value: 9, label: "其他" },
];

export const mediaTypeOptions = [
  { value: 1, label: "文本" },
  { value: 2, label: "静图" },
  { value: 3, label: "动图" },
  { value: 5, label: "视频" },
  { value: 6, label: "链接" },
];

export const contentTypeLabelMap = Object.fromEntries(
  contentTypeOptions.map((item) => [item.value, item.label]),
);

export const mediaTypeLabelMap = Object.fromEntries(
  mediaTypeOptions.map((item) => [item.value, item.label]),
);

export const mediaGroupByType = {
  1: "text",
  2: "image",
  3: "image",
  4: "image",
  5: "video",
  6: "link",
};

export const mediaTypeByGroup = {
  text: 1,
  image: 2,
  video: 5,
  link: 6,
};
