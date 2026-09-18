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
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.1/STCore.xcframework.zip",
            checksum: "7980cbcbd1bf057b7ab38a7e657097268431da6e9f65f2a75a68ede507f970af"
        ),
        .binaryTarget(
            name: "Database",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.1/STDatabase.xcframework.zip",
            checksum: "4871a3b846da893a085dfbbe2f3af76f4b050e697aff075a8fc08944afed6f44"
        ),
        .binaryTarget(
            name: "Network",
            url: "https://github.com/Infomaniak/multiplatform-SwissTransfer/releases/download/11.0.1/STNetwork.xcframework.zip",
            checksum: "ded43041decc14cb4587de1b7f9e827b39b420d5799184f9d1362d6605fc1f92"
        ),
    ]
)
