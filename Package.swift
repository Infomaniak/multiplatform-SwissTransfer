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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.8/STCore.xcframework.zip",
            checksum: "95b6630badfbb1af62348aced9e70d4e56b458ac3b0d07021708c1ca584f96cd"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.8/STDatabase.xcframework.zip",
            checksum: "1d7549532c5ad0e87b160b1bcc7e363c72349555208cd02eb4153ee9e64002fa"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/1.0.8/STNetwork.xcframework.zip",
            checksum: "f180a2cc20cf029467fe1db62dc80b938055ef8603d87a86913c80c650093e37"
        ),
    ]
)
