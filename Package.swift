// swift-tools-version:5.10
import PackageDescription

let package = Package(
    name: "SwissTransfer-Multiplatform",
    platforms: [
        .iOS(.v14),
        .macOS(.v11)
    ],
    products: [
        .library(name: "STCore", targets: ["Core"]),
        .library(name: "STDatabase", targets: ["Database"]),
        .library(name: "STNetwork", targets: ["Network"])
    ],
    targets: [
        .binaryTarget(
            name: "Core",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.3-bundleid-202608200747-32345246352-1/STCore.xcframework.zip",
            checksum: "7f14e359f60af602304dc97fe0cfdc604b39cbe5ef4c685265e5c0adf31750cb"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.3-bundleid-202608200747-32345246352-1/STDatabase.xcframework.zip",
            checksum: "2cda8961eaa38658d6a8ba87ebfd0249064fc93a26ecde545f9db8967399091c"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-10.1.3-bundleid-202608200747-32345246352-1/STNetwork.xcframework.zip",
            checksum: "5aeb39bca2ac93ac2da59f709a69b55d850b3f210815725e62c19ed31502c7b0"
        ),
    ]
)
