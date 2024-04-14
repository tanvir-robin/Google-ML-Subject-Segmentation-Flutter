import 'dart:io';
import 'package:image/image.dart' as img;
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:image_picker/image_picker.dart';
import 'package:scanning_effect/scanning_effect.dart';

void main() {
  runApp(MaterialApp(
      theme: ThemeData(useMaterial3: false), home: const HomePage()));
}

class HomePage extends StatefulWidget {
  const HomePage({super.key});

  @override
  State<HomePage> createState() => _HomePageState();
}

class _HomePageState extends State<HomePage> {
  Uint8List? finalResult;
  bool isLoaded = false;
  bool processing = false;
  File? pickedImage;
  bool startAnimation = false;
  final MethodChannel channel =
      const MethodChannel('subject_segmentation_channel');

  Future<void> pickImageFromGallery() async {
    final picker = ImagePicker();
    XFile? tmp = await picker.pickImage(source: ImageSource.gallery);
    if (tmp == null) {
      return;
    }
    pickedImage = File(tmp.path);
    setState(() {
      isLoaded = false;
    });
  }

  Future<void> processImage() async {
    setState(() {
      processing = true;
    });
    Uint8List bytes = await pickedImage!.readAsBytes();
    final argument = {'imageBytes': bytes};
    final Uint8List result =
        await channel.invokeMethod('segmentImage', argument);
    img.Image? pp = img.decodeImage(result);
    if (pp == null) {
      return;
    } else {
      finalResult = result;
    }
    setState(() {
      isLoaded = true;
      processing = false;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: const Text(
          'Google ML Implementation',
          style: TextStyle(color: Colors.white),
        ),
        backgroundColor: const Color.fromRGBO(67, 24, 255, 1),
        shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(20)),
      ),
      body: Center(
        child: SingleChildScrollView(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              if (pickedImage != null && !isLoaded)
                SizedBox(
                  height: MediaQuery.of(context).size.height * .30,
                  width: MediaQuery.of(context).size.width * .90,
                  child: processing
                      ? ScanningEffect(
                          scanningColor: Colors.yellow,
                          borderLineColor: Colors.white,
                          delay: const Duration(seconds: 0),
                          duration: const Duration(seconds: 2),
                          child: Image.file(
                            pickedImage!,
                            fit: BoxFit.cover,
                            height: MediaQuery.of(context).size.height * .30,
                            width: MediaQuery.of(context).size.width * .75,
                          ))
                      : Image.file(
                          pickedImage!,
                          fit: BoxFit.cover,
                          height: MediaQuery.of(context).size.height * .30,
                          width: MediaQuery.of(context).size.width * .75,
                        ),
                ),
              if (isLoaded)
                Image.memory(
                  finalResult!,
                  fit: BoxFit.cover,
                  width: MediaQuery.of(context).size.width * .90,
                ),
              ElevatedButton(
                  style: ElevatedButton.styleFrom(
                      backgroundColor: const Color.fromRGBO(67, 24, 255, 1)),
                  onPressed: pickedImage == null || isLoaded == true
                      ? pickImageFromGallery
                      : processImage,
                  child: Text(
                    pickedImage == null || isLoaded == true
                        ? 'Pick an Image'
                        : 'Process Image',
                    style: const TextStyle(fontSize: 17, color: Colors.white),
                  ))
            ],
          ),
        ),
      ),
    );
  }
}
