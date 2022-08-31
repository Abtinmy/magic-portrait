# Magic Portrait

## Table of Content
* [Demo](#demo)
* [Overview](#overview)
* [Technical Aspect](#technical-aspect)
* [Directory Tree](#directory-tree)
* [To Do](#to-do)
* [Bug / Feature Request](#bug---feature-request)
* [Technologies Used](#technologies-used)
* [License](#license)
* [Credits](#credits)


## Demo and Installation
Download the app from [Bazzar](), [Myket](), [Github](), or [Google Drive](https://drive.google.com/file/d/1_9cmIjUrZ369ZzrYRbzZt7MwmEX-suXW/view?usp=sharing). 

![](images/demo.png)

## Overview
In this application with the help of artificial intelligence and deep learning, two deep models have been created. First one is a segmentation model which can mask out every pet that have appeared in the original image. Second one is a style transfer model which can change style of the original image to the desired style which can be a painting from a famous artist or a natural texture (like wood).

Then, the obtained images from these models combined into a single image using Poisson image editing, in a way that, the background of the original image stays unchanged, and pets recognized from segmentation model changed to style transformed image.

## Technical Aspect
This project is divided into four parts:
1. Training a deep segmentation model.
2. Training a deep style transfer model.
3. Saved the trained models in TFLite format.
4. Building an android app.
    - User can choose image from devices' gallery, capture it using devices' camera, or use the sample pre-defined image.
    - Use saved TFLite segmentation model to mask out every pet that have been appeared in the image.
    - Use saved TFLite style transfer model to transform the style of the content image to desired style. (user can choose one of the 10 pre-defined styles or import a new style from devices' gallery.)
    - Combine the obtained images from these models using Poisson image editing.
    - Save the resulted image in devices' storage.

### Segmentation
I used a modified [U-net](https://en.wikipedia.org/wiki/U-Net) model for segmentation task. U-net is a auto-encoder that in its' first half there is a encoder which transforms the original image into a different representational space, and in the second half there is a decoder which transformes the results form the encoder to the target data. For training, I used [The Oxford-IIIT Pet Dataset](https://www.robots.ox.ac.uk/~vgg/data/pets/).

For encoder, I used [mobilenet-v2](https://arxiv.org/abs/1801.04381) architecture, and for decoder, upsamples block from [pix2pix](https://arxiv.org/abs/1611.07004) model. In the training phase, encoder freezed and only weights of the decoder updated.The resulted model can be seen in the following image.

![](images/model-segmentation.png)

### Style Transfer
For style transfer model I used pretrained [VGG-19](https://arxiv.org/abs/1409.1556) as the base which content and style images are be fitted into. Some blocks in this model are dedicated to style image and some for content image. representation from these blocks are gathered into one single representation for both style and content images. Then by calculating the mean square error for the models' output relative to each target, then take the weighted sum of these losses. finally, using gradient descent, we apply the gradients to the image. The resulted model can be seen in the following image.

![](images/model-style-transfer.png)

### Image Blending
For blending the obtained images form these models, I used [Poisson image editing](https://en.wikipedia.org/wiki/Gradient-domain_image_processing#:~:text=Gradient%20domain%20image%20processing%2C%20also,on%20the%20pixel%20values%20directly.) technique which is a type of digital image processing that operates on the differences between neighboring pixels, rather than on the pixel values directly.  In gradient domain methods one fixes the colors of the boundary (taken from the background image) and provides a vector field that defines the structure of the image to be copied (taken from the foreground and / or a mixture from foreground and background). The result image is generated by minimizing the squared error terms between the gradient of the result image and the guidance vector field.

The complete workflow is demonstrated in the following diagram.
![](images/diagram.png)

## Directory Tree
```
├── app 
│     ├── src
│     │      ├── main
│     │      │      ├── cpp
│     │      │      │     ├── SeamlessBlending
│     │      │      │     ├── CMakeLists.txt
│     │      │      │     └── native-lib.cpp
│     │      │      ├── ml
│     │      │      │     ├── segmentor.tflite
│     │      │      │     ├── stylemodel.tflite
│     │      │      │     └── transformer.tflite
│     │      │      ├── res
│     │      │      │     └── drawables
│     │      │      ├── java
│     │      │      │      ├── LoadingDialog.java
│     │      │      │      ├── MainActivity.java
│     └──    └──    └──    └── TransformerActivity.java
├── LICENSE
├── build.gradle
├── settings.gradle
├── .gitignore
└── README.md
```

## To Do
1. Parallelize the Poisson image editing proccess in order to speed up the application.
2. Extend the segmentation model to mask out various types of objects.
3. Build and train a deep neural network to blend the resulted images more accurately, for speed up and better performance.

## Bug / Feature Request
If you find a bug, kindly open an issue [here]() by including your inputs and the expected result.

If you'd like to request a new function, feel free to do so by opening an issue [here](). Please include sample inputs and their corresponding results.

## Technologies Used

<img src="images/tensorflow.png" width="200px" height="200px"><img src="images/tflite.png" width="200px" height="200px"><img src="images/python.png" width="200px" height="200px"><img src="images/keras.png" width="200px" height="200px"><img src="images/java.png" width="200px" height="200px"><img src="images/cpp.png" width="200px" height="200px"><img src="images/opencv.png" width="200px" height="200px"><img src="images/Eigen.png" width="200px" height="200px"><img src="images/android.png" width="200px" height="200px">

## License

## Credits
- [Segmentation tutorial from Tensorflow](https://www.tensorflow.org/tutorials/images/segmentation)
- [Style Transfer tutorial from Tensorflow](https://www.tensorflow.org/tutorials/generative/style_transfer)
- [The Oxford-IIIT Pet Dataset for segmentation task](https://www.robots.ox.ac.uk/~vgg/data/pets/)
- [Poisson image editing](https://github.com/cheind/poisson-image-editing)
- [TFLite tutorials from Tensorflow](https://www.tensorflow.org/lite/models)
- [Android guidlines and Tutorials](https://developer.android.com/docs)