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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-scoping-202610060828-37435581630-1/STCore.xcframework.zip",
            checksum: "dbe6417de7f8717f7f66aa9ed726f54919c40f369b7e94782aedd77a85ff4b55"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-scoping-202610060828-37435581630-1/STDatabase.xcframework.zip",
            checksum: "e9e555a42e95e637b164b83daeeb82e06a87ccfadb7094773641ecac1ccc86d0"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/ios-snapshot-12.0.0-scoping-202610060828-37435581630-1/STNetwork.xcframework.zip",
            checksum: "a342f29cfcff465a96c753e862d85ea4e8523d372ab75c547800cb88f4facb58"
        ),
    ]
)
