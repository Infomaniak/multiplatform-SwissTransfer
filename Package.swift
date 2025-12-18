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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.0.0/STCore.xcframework.zip",
            checksum: "957f9023d44bc6486c382d39725a5b59276b24907eb7717c9eb962feb97cb7ff"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.0.0/STDatabase.xcframework.zip",
            checksum: "f8693fc4ddb683974cfb59aa7daae4fe4377bf82b63dc68676481a73d51f6cd9"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/6.0.0/STNetwork.xcframework.zip",
            checksum: "377c45dc53d644a367dbd9c2d21c876432cdf87e26f4e74dd05b7b2221cc5b27"
        ),
    ]
)
